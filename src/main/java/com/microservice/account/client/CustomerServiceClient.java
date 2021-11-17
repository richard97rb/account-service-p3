package com.microservice.account.client;

import com.microservice.account.config.AccountServiceConfig;
import com.microservice.account.entities.dtos.CustomerDto;
import com.microservice.account.entities.dtos.ResponseCustomerDto;
import com.microservice.account.entities.dtos.ResponseSignerDto;
import com.microservice.account.entities.dtos.SignerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CustomerServiceClient {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private AccountServiceConfig config;

    public List<ResponseCustomerDto> createCustomers(List<CustomerDto> dtos){
        List<ResponseCustomerDto> result = new ArrayList<>();
        //Heders
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //Content
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(dtos,headers);
        result =  restTemplate.exchange(config.getCustomerServiceUrl()+"/createCustomers",
                HttpMethod.POST, requestEntity, new ParameterizedTypeReference<List<ResponseCustomerDto>>() {
                }).getBody();
        log.info("Response:" + requestEntity.getHeaders());
        return result;
    }

    public List<ResponseSignerDto> createSigners(List<SignerDto> dtos){
        List<ResponseSignerDto> result = new ArrayList<>();
        //Heders
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //Content
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(dtos,headers);
        result =  restTemplate.exchange(config.getCustomerServiceUrl()+"/createSigners",
                HttpMethod.POST, requestEntity, new ParameterizedTypeReference<List<ResponseSignerDto>>() {
                }).getBody();
        log.info("Response:" + requestEntity.getHeaders());
        return result;
    }


    public List<ResponseCustomerDto> findCustomerByDni(List<String> dnis){
        List<ResponseCustomerDto> result = new ArrayList<>();
        try{
            //Heders
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            //Content
            HttpEntity<Object> requestEntity = new HttpEntity<Object>(dnis,headers);
            result =  restTemplate.exchange(config.getCustomerServiceUrl() + "/findCustomers",
                       HttpMethod.POST, requestEntity, new ParameterizedTypeReference<List<ResponseCustomerDto>>() {
            }).getBody();
        }catch (HttpClientErrorException ex){
            if(ex.getStatusCode() != HttpStatus.NOT_FOUND){
                throw  ex;
            }
        }
        return result;
    }


}
