package com.microservice.account.config;


import com.microservice.account.entities.AccountType;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

enum AccountTypeEnum{
    AHORRO(AccountType.builder()
            .type("AHORRO")
            .commissions(0.00)
            .maxTransactionsPerMonth(500)
            .productType("PASIVOS")
            .build()
    ),
    CORRIENTE(AccountType.builder()
            .type("CORRIENTE")
            .commissions(0.20)
            .maxTransactionsPerMonth(2)
            .productType("PASIVOS")
            .build()
    ),
    PLAZO_FIJO(AccountType.builder()
            .type("PLAZO_FIJO")
            .commissions(0.2)
            .maxTransactionsPerMonth(1)
            .productType("PASIVOS")
            .build()
    ),
    PERSONAL(AccountType.builder()
            .type("PERSONAL")
            .commissions(0.00)
            .maxTransactionsPerMonth(500)
            .productType("ACTIVOS")
            .build()
    ),
    EMPRESARIAL(AccountType.builder()
            .type("EMPRESARIAL")
            .commissions(0.00)
            .maxTransactionsPerMonth(500)
            .productType("ACTIVOS")
            .build()
    ),
    TARJETA_CREDITO(AccountType.builder()
            .type("TARJETA_CREDITO")
            .commissions(0.00)
            .maxTransactionsPerMonth(500)
            .productType("ACTIVOS")
            .build()
    ),
    TARJETA_DEBITO(AccountType.builder()
            .type("TARJETA_DEBITO")
            .commissions(0.00)
            .maxTransactionsPerMonth(99999)
            .productType("ACTIVOS")
            .build()
    );

    private AccountType type;

    AccountTypeEnum(AccountType type){
        this.type = type;
    }

    public AccountType getType(){return this.type;};

}

@Component
public class AppConfig {

    public Optional<AccountType> getAccountTypeByName(String name){
        AccountTypeEnum[] accountTypes = AccountTypeEnum.values();
        Optional<AccountTypeEnum> typeEnum = Arrays.stream(accountTypes)
                .filter(x-> x.getType().getType().equals(name))
                .findFirst();

        return Optional.ofNullable(typeEnum.get().getType());
    }
}
