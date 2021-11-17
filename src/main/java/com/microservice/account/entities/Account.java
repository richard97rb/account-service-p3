package com.microservice.account.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "accounts")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account {
    @Id
    private ObjectId _id;

    private Double balance;

    @Field("account_number")
    private String accountNumber;

    @Field("creation_date")
    private LocalDate creationDate;
    
    @Field("customers_ids")
    private List<ObjectId> customersIds;

    @Field("signers_ids")
    private List<ObjectId> signerIds;

    @Field("account_type")
    private AccountType accountType;
    
    /* Negocio 3*/
    @Field("accounts_ids")
    private List<String> accountsIds;
    
    @Field("principal_account")
    private boolean principalAccount;

}
