package com.microservice.account.controller;

import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.microservice.account.controllers.AccountController;
import com.microservice.account.entities.Account;
import com.microservice.account.entities.AccountType;
import com.microservice.account.entities.dtos.AccountDto;
import com.microservice.account.entities.dtos.ConsultAccountDto;
import com.microservice.account.entities.dtos.CreateAccountDto;
import com.microservice.account.entities.dtos.CustomerDto;
import com.microservice.account.entities.dtos.ResponseAccountDto;
import com.microservice.account.entities.dtos.SignerDto;
import com.microservice.account.entities.dtos.TransactionDto;
import com.microservice.account.services.IAccountService;

@ExtendWith(SpringExtension.class)
@WebFluxTest(AccountController.class)
public class AccountControllerTest {

	@Autowired
	WebTestClient webTestClient;
	
	@MockBean
	private IAccountService accountService;
	
	public static final ModelMapper modelMapper = new ModelMapper();
	
	@Test
	public void createAccount() throws Exception {
		
		List<CustomerDto> customers = Arrays.asList(CustomerDto.builder()
				.name("Christian")
				.lastName("Garcia")
				.dni("72468245")
				.build());
		
		List<SignerDto> signers = Arrays.asList(SignerDto.builder()
				.name("Marcelo")
				.lastName("Garcia")
				.dni("72468246")
				.build());
		
		AccountDto account = AccountDto.builder()
								.type("AHORRO")
								.build();
		
		CreateAccountDto body = CreateAccountDto.builder()
										.account(account)
										.customers(customers)
										.signers(signers)
										.build();
		
		Account result = Account.builder()
                .balance(0.00)
                .accountNumber("62949091864873274934")
                .creationDate(LocalDate.now())
                .customersIds(Arrays.asList(new ObjectId("6189ab694dedba3b28cc1132")))
                .signerIds(Arrays.asList(new ObjectId("6189ab694dedba3b28cc1133")))
                .accountType(AccountType.builder()
                		.type("AHORRO")
                		.commissions(0.0)
                		.maxTransactionsPerMonth(500)
                		.productType("PASIVOS")
                		.build())
                .accountsIds(null)
                .principalAccount(true)
                .build();
		
		ResponseAccountDto accountResponse = modelMapper.map(result , ResponseAccountDto.class);
		
		when(accountService.createAccount(body)).thenReturn(accountResponse);

		webTestClient.post()
			.uri("/api/accounts/")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isOk()
			.expectBody(ResponseAccountDto.class);
		
	}
	
	@Test
	public void findAccountByAccountNumberTest() throws Exception {
		
		ObjectId id = new ObjectId();
		
		ResponseAccountDto response = ResponseAccountDto.builder()
										._id(id.toString())
										.balance(0.0)
										.accountNumber("62949091864873274934")
										.customersIds(Arrays.asList("6189ab694dedba3b28cc1132", "6189ab694dedba3b28cc1133"))
										.accountType(AccountType.builder()
												.type("AHORRO")
												.build())
										.build();
		
		when(accountService.findAccountByAccountNumber("62949091864873274934")).thenReturn(response);
		
		webTestClient.get()
			.uri("/api/accounts/findByAccountNumber/62949091864873274934")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectBody(ResponseAccountDto.class);
		
	}
	
	@Test
	public void updateAmountTest() throws Exception {
		
		TransactionDto transaction = TransactionDto.builder()
										.amount(10.00)
										.commission(0.0)
										.transactionType("DEPOSITO")
										.accountId("618aaefeb3a82944b2c659c7")
										.transactionDate(LocalDateTime.now())
										.build();
		
		ResponseAccountDto response = ResponseAccountDto.builder()
										.balance(245.0)
										.accountNumber(transaction.getAccountId())
										.customersIds(Arrays.asList("6189b0e24dedba3b28cc1138", "6189b0e24dedba3b28cc1139"))
										.accountsIds(null)
										.accountType(AccountType.builder()
												.type("CORRIENTE")
												.commissions(0.2)
												.maxTransactionsPerMonth(2)
												.productType("PASIVOS")
												.build())
										.build();
				
		when(accountService.updateAmount(transaction, "73582232028410884175")).thenReturn(response);
		
		webTestClient.put()
			.uri("/api/accounts/updateAmount/73582232028410884175")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.bodyValue(transaction)
			.exchange()
			.expectStatus().isOk()
			.expectBody(ResponseAccountDto.class);						
		
	}
	
	@Test
	public void consultAccountTest() throws Exception {
		
		TransactionDto transaction = TransactionDto.builder()
										.amount(10.00)
										.commission(0.0)
										.transactionType("DEPOSITO")
										.accountId(new ObjectId().toString())
										.transactionDate(LocalDateTime.now())
										.build();
		
		ConsultAccountDto consult = ConsultAccountDto.builder()
										.balance(0.0)
										.transactions(Arrays.asList(transaction))
										.build();
		
		when(accountService.consultAccount("73582232028410884175")).thenReturn(consult);
		
		webTestClient.get()
			.uri("/api/accounts/consultAccountByAccountNumber/73582232028410884175")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectBody(ConsultAccountDto.class);
	}
	
}
