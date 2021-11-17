package com.microservice.account.services.impl;

import com.microservice.account.client.CustomerServiceClient;
import com.microservice.account.client.TransactionServiceClient;
import com.microservice.account.config.AppConfig;
import com.microservice.account.entities.Account;
import com.microservice.account.entities.AccountType;
import com.microservice.account.entities.dtos.*;
import com.microservice.account.repositories.IAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccountServiceImpl implements com.microservice.account.services.IAccountService {

    @Autowired
    private CustomerServiceClient customerClient;

    @Autowired
    private TransactionServiceClient transactionClient;

    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    private  AppConfig appConfig;

    public static final ModelMapper modelMapper=new ModelMapper();

    private String createRandomAccountNumber(){
        Random rand = new Random();
        StringBuilder card = new StringBuilder();
        for (int i = 0; i < 20; i++)
        {
            int n = rand.nextInt(10);
            card.append(Integer.toString(n));
        }
        return card.toString();
    }

    private boolean validateCustomerHaveCreditCard(CreateAccountDto dto, List<ResponseCustomerDto> customers) throws Exception {
    	
    	if(customers.size() > 0) { // Si alguno de los clientes ya existe, verificar que tenga tarjeta de crédito
    		List<ObjectId> customersId = new ArrayList<>();
        	customers.forEach(customer -> { customersId.add(customer.get_id()); });
        	
        	List<ResponseAccountDto> result = findAccountByCustomersIdsIn(customersId).stream()
        				.filter(acc -> acc.getAccountType().getType().equals("TARJETA_CREDITO")).collect(Collectors.toList()); //.toList();
        		
        	if(result.size() > 0) {
        		return true;
        	} else {
        		log.info("Alguno de los clientes existentes no posee una tarjeta de crédito.");
        		return false;
        	}
        } else {
        	// Else si todos son nuevos, indicar que alguno se cree una tarjeta de credito primero
        	log.info("Crearse una tarjeta de crédito primero para acceder a la cuenta ahorro vip.");
        	return false;
        }
	}
    
    public void validateSpecialAccount(CreateAccountDto dto, List<ResponseCustomerDto> customers, String customerType, 
    		String accountType) throws Exception {
    	
    	// Si hay al menos un cliente personal_vip y se crea una cuenta tipo "ahorro"
        if(dto.getCustomers().stream().filter(customer -> customer.getType().getName().equals(customerType)).count() > 0
        		&& dto.getAccount().getType().equals(accountType)) {
        	if(!validateCustomerHaveCreditCard(dto, customers)) { // Si no tiene tarjeta de crédito  == false
        		throw new Exception("No es posible crear la cuenta de tipo " + accountType); 
        	}
        }; // Si tiene qué continue con la creación de la cuenta
    
    }
    
    // Siempre que crees un "ACTIVOS" añadir "maxLimitCredit" y "expirationCreditDate" siempre o se cae.
    private boolean haveOverdueDebt(List<ObjectId> customersId) throws Exception {
        List<ResponseAccountDto> overdueDebts = findAccountByCustomersIdsIn(customersId).stream()
                .filter(acc -> acc.getAccountType().getProductType().equals("ACTIVOS") && !acc.getAccountType().getType().equals("TARJETA_DEBITO"))
                .filter(acc -> acc.getAccountType().getExpirationCreditDate().isBefore(LocalDate.now()))
                .filter(acc -> acc.getBalance() != 0)
                .collect(Collectors.toList());
        return overdueDebts.size() != 0;
    }

    public boolean existsDebitCard(List<ObjectId> customersId) throws Exception {
    	
    	//Limitar a que tenga una tarjeta
    	List<ResponseAccountDto> filter = findAccountByCustomersIdsIn(customersId).stream()
				.filter(acc -> acc.getAccountType().getType().equals("TARJETA_DEBITO")).collect(Collectors.toList());
    	if(filter.size() > 0)
    		return true;
    	else
    		return false;
    	
    }

    @Override
    public ResponseAccountDto createAccount(CreateAccountDto dto) throws Exception {
        List<ResponseCustomerDto> customers = new ArrayList<>();

        //Make dni array
        List<String> dnis = new ArrayList<>();
        dto.getCustomers().forEach(customer->{
            dnis.add(customer.getDni());
        });
        
        //Customers that exists
        customers = customerClient.findCustomerByDni(dnis);
        
        //Customers Ids for bussiness rules
    	List<ObjectId> customersId = new ArrayList<>();
    	customers.forEach(customer -> { customersId.add(customer.get_id()); });

        //validate overdueDebt
        if (haveOverdueDebt(customersId)) throw new Exception("Usted posee deuda(s) vencida(s).");

        //Validate business rules
        validateSpecialAccount(dto, customers, "PERSONAL_VIP", "AHORRO");
        validateSpecialAccount(dto, customers, "EMPRESARIAL_PYME", "CORRIENTE");
        
        //Clear dnis and add only dnis of customers found
        dnis.clear();
        customers.forEach(customer->{
            dnis.add(customer.getDni());
        });

        //Add new customers
        List<CustomerDto> customersMissing = new ArrayList<>();
        dto.getCustomers().forEach(customer->{
            if(!dnis.contains(customer.getDni())){
                customersMissing.add(customer);
            }
        });
        customers.addAll(customerClient.createCustomers(customersMissing));

        //Get customers ids after save
        List<ObjectId> ids = new ArrayList<>();
        customers.forEach(finalCustomer->{
            ids.add(finalCustomer.get_id());
        });

        //Add new signers
        List<ResponseSignerDto> signers = new ArrayList<>();
        signers = customerClient.createSigners(dto.getSigners());

        //Gte signers ids after save
        List<ObjectId> signersIds = new ArrayList<>();
        signers.forEach(finalSigner->{
            signersIds.add(finalSigner.get_id());
        });
        
        //Create account - If is a special account and i got here, it is because it met all the conditions
        Optional<AccountType> accTypeOptional = appConfig.getAccountTypeByName(dto.getAccount().getType());
        
        if(accTypeOptional.isPresent()){
            
        	List<ResponseAccountDto> result = findAccountByCustomersIdsIn(customersId).stream()
                    .filter(acc -> acc.getAccountType().getProductType().equals("PASIVOS"))
                    .collect(Collectors.toList());
        	
        	AccountType accountType = accTypeOptional.get();
            Boolean principal = false;

        	if(dto.getAccount().getType().equals("EMPRESARIAL_PYME")) { accountType.setCommissions(0.0); }
        	//if(dto.getAccount().getType().equals("PERSONAL_VIP")) { } 
 
        	if(accountType.getProductType().equals("ACTIVOS") && !accountType.getType().equals("TARJETA_DEBITO")) {
        		if(dto.getAccount().getExpirationCreditDate() != null && dto.getAccount().getMaxLimitCredit() != null) {
        			accountType.setExpirationCreditDate(dto.getAccount().getExpirationCreditDate());
        			accountType.setMaxLimitCredit(dto.getAccount().getMaxLimitCredit());
        		} else {
        			throw new Exception("Los activos deben incluir los campos de limite maximo de crédito y fecha de vencimiento de la deuda.");
        		}
        	}
        	
        	if(result.size() == 0) principal = true; // If I create a "PASIVOS" account for the first time, it is created as the main account

        	//Number of account to create
        	String accountNumber = createRandomAccountNumber();
        	
        	//Asocciate accounts to debit card
        	List<String> numberAccounts = new ArrayList<>();
            if(!existsDebitCard(customersId)) { //If i don't have debit card
            	if(dto.getAccount().getType().equals("TARJETA_DEBITO")) { //And the type to create is debit card
            		
                	//Associate existing accounts
            		result.forEach(res -> numberAccounts.add(res.getAccountNumber())); 
            		
            		//Set up main account for debit card
//            		String numAccountPrincipal = result.get(0).getAccountNumber();
//            		updatePrincipalAccount(numAccountPrincipal);
            		
            	} // And it is another type of account, nothing happens.
            } else { // But if i have a debit card
            	if(accountType.getProductType().equals("PASIVOS")) { //And I try to create some kind of bank account
            		
            		// Update ids in debit card (accountsIds)
            		updateAccountsIdsForDebitCard(customersId, accountNumber);
            		
            	} else if(dto.getAccount().getType().equals("TARJETA_DEBITO")) {
            		throw new Exception("Ya tienes una tarjeta de débito.");
            	}
            }

            Account account = Account.builder()
                    .balance(0.00)
                    .accountNumber(accountNumber)
                    .creationDate(LocalDate.now())
                    .customersIds(ids)
                    .signerIds(signersIds)
                    .accountType(accountType)
                    .accountsIds(numberAccounts) //Only has value for debit card
                    .principalAccount(principal)
                    .build();
            account = accountRepository.save(account);
            
            ResponseAccountDto response = modelMapper.map(account,ResponseAccountDto.class);
            return  response;
        }else{
            throw new Exception("ACC_TYPE_NOT_FOUND");
        }
    }

    @Override
    public ResponseAccountDto findAccountByAccountNumber(String accountNumber) throws Exception {

        Account account = accountRepository.findAccountByAccountNumber(accountNumber)
                .orElseThrow(() -> new Exception("ACCOUNT_NOT_FOUND"));
        ResponseAccountDto response =  modelMapper.map(account,ResponseAccountDto.class);
        return response;
    }

    @Override
    public ResponseAccountDto updateAmount(TransactionDto dto, String accountId) throws Exception {
        //Validate that account exists
        Account account = accountRepository.findById(new ObjectId(accountId))
                .orElseThrow(()->new Exception("ACCOUNT_NOT_FOUND"));
        //Update amount
        switch (dto.getTransactionType()){
            case "DEPOSITO": 
            case "TRANSFER_RECIBIDO": 
                account.setBalance(account.getBalance() + dto.getAmount() - (dto.getAmount()*dto.getCommission()));
                accountRepository.save(account);
                break;
            case "RETIRO": case "PAGO": 
            case "TRANSFER_ENVIO": case "ENVIADO_PAGO_CREDITO_TERCERO": case "RECIBIDO_PAGO_CREDITO_TERCERO": 
                account.setBalance(account.getBalance() - dto.getAmount() - (dto.getAmount()*dto.getCommission()));
                accountRepository.save(account);
                break;
        }

        ResponseAccountDto response = modelMapper.map(account,ResponseAccountDto.class);

        return response;
    }

    @Override
    @Transactional
    public ConsultAccountDto consultAccount(String accountNumber) throws Exception {
        Account account = accountRepository.findAccountByAccountNumber(accountNumber)
                .orElseThrow(()->new Exception("ACCOUNT_NOT_FOUND"));

        //Get transactions
        System.out.println(account.get_id().toString());
        List<TransactionDto> transactions = transactionClient.findTransactionsByAccountId(account.get_id().toString());
        ConsultAccountDto response = ConsultAccountDto.builder()
                .balance(account.getBalance())
                .transactions(transactions)
                .build();

        return  response;

    }

    @Override
    public Double consultBalanceOfPrincipalAccount(String debitCardNumber) throws Exception {
        Account debitCard = accountRepository.findAccountByAccountNumber(debitCardNumber).filter(account -> account.getAccountType().getType().equals("TARJETA_DEBITO"))
                .orElseThrow(()->new Exception("DEBIT_CARD_NOT_FOUND"));
        List<Account> accounts = new ArrayList<>();
        debitCard.getAccountsIds().stream().forEach(accountNumber -> {
            accounts.add(accountRepository.findAccountByAccountNumber(accountNumber).get());
        });
        Account principalAccount = accounts.stream().filter(acc -> acc.isPrincipalAccount()).collect(Collectors.toList()).get(0);
        return principalAccount.getBalance();
    }

	private List<ResponseAccountDto> findAccountByCustomersIdsIn(List<ObjectId> customersIds) throws Exception {
		List<Account> accounts = accountRepository.findAccountByCustomersIdsIn(customersIds);
		List<ResponseAccountDto> response = new ArrayList<>();
		accounts.forEach(account -> {
			response.add(modelMapper.map(account, ResponseAccountDto.class));
		});
		return response;
	}

    public List<ResponseAccountDto> findAccountByCustomersDnis(List<String> customersDnis) throws Exception {
        List<ResponseCustomerDto> customers = new ArrayList<>();
        customers = customerClient.findCustomerByDni(customersDnis);
        List<ObjectId> customersId = new ArrayList<>();
        customers.forEach(customer -> { customersId.add(customer.get_id()); });
        List<Account> accounts = accountRepository.findAccountByCustomersIdsIn(customersId);
        List<ResponseAccountDto> response = new ArrayList<>();
        accounts.forEach(account -> {
            response.add(modelMapper.map(account, ResponseAccountDto.class));
        });
        return response;
    }

	@Override
	public ResponseAccountDto updatePrincipalAccount(String accountNumber) throws Exception {
		
		//Validate if account exists
		Account account = accountRepository.findAccountByAccountNumber(accountNumber)
	                .orElseThrow(() -> new Exception("ACCOUNT_NOT_FOUND"));
		
        //Update account to principal for debit card
        account.setPrincipalAccount(true);
        accountRepository.save(account);
        
        ResponseAccountDto response = modelMapper.map(account,ResponseAccountDto.class);
        return response;
	}

	@Override
	public ResponseAccountDto updateAccountsIdsForDebitCard(List<ObjectId> customersId, String accountNumber)
			throws Exception {
		
		Account debit = accountRepository.findAccountByCustomersIdsIn(customersId).stream()
				.filter(acc -> acc.getAccountType().getType().equals("TARJETA_DEBITO"))
				.collect(Collectors.toList()).get(0);
		
		List<String> newAccountIds = debit.getAccountsIds();
		newAccountIds.add(accountNumber);
		debit.setAccountsIds(newAccountIds); //Add new number to the associated accounts
		accountRepository.save(debit); //Update debit card, but only the account numbers
		
		ResponseAccountDto response = modelMapper.map(debit, ResponseAccountDto.class);
        return response;
	}
	
}
