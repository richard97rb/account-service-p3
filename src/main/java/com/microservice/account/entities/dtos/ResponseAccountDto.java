package com.microservice.account.entities.dtos;

import com.microservice.account.entities.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseAccountDto {

    private String _id;

    private Double balance;

    private String accountNumber;

    private List<String> customersIds;
    
    private List<String> accountsIds;
    
    private AccountType accountType;
}
