package com.microservice.account.entities.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDto {
    private Double amount;
    
    private Double commission;
    
    private String transactionType;

    private String accountId;
    
    private LocalDateTime transactionDate;
}
