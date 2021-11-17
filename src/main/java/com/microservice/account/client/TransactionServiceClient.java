package com.microservice.account.client;

import com.microservice.account.config.AccountServiceConfig;
import com.microservice.account.entities.dtos.ResponseCustomerDto;
import com.microservice.account.entities.dtos.TransactionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TransactionServiceClient {
    @Autowired
    private AccountServiceConfig config;

    @Autowired
    RestTemplate restTemplate;

    public List<TransactionDto> findTransactionsByAccountId(String accountId){
        List<TransactionDto> result = new ArrayList<>();
        try{
            result =  restTemplate.exchange(config.getTransactionServiceUrl()+"/accountId/{accountId}",
                    HttpMethod.GET, null, new ParameterizedTypeReference<List<TransactionDto>>() {
                    },accountId).getBody();
        }catch (HttpClientErrorException ex){
            if(ex.getStatusCode() != HttpStatus.NOT_FOUND){
                throw  ex;
            }
        }
        return result;
    }


}
