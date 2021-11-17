package com.microservice.account.controllers;

import com.microservice.account.entities.dtos.*;
import com.microservice.account.services.IAccountService;

import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", methods = { RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT,
        RequestMethod.DELETE })
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    IAccountService accountService;

    @PostMapping()
    public Mono<ResponseAccountDto> createAccount(@Validated @RequestBody CreateAccountDto dto) throws Exception {
        return Mono.just(accountService.createAccount(dto));
    }

    @GetMapping(value = "findByAccountNumber/{accountNumber}")
    public Mono<ResponseAccountDto> findAccountByAccountNumber(@PathVariable String accountNumber) throws Exception {
        return Mono.just(accountService.findAccountByAccountNumber(accountNumber));
    }

    @PutMapping(value = "updateAmount/{accountId}")
    public Mono<ResponseAccountDto> updateAmount(@Validated @RequestBody TransactionDto dto, @PathVariable String accountId) throws Exception {
        return Mono.just(accountService.updateAmount(dto,accountId));
    }

    @GetMapping("consultAccountByAccountNumber/{accountNumber}")
    public Mono<ConsultAccountDto> consultAccount(@PathVariable String accountNumber) throws Exception {
        return Mono.just(accountService.consultAccount(accountNumber));
    }


    @GetMapping("{debitCardNumber}/balance")
    public Mono<Double> consultBalanceOfPrincipalAccount(@PathVariable String debitCardNumber) throws Exception {
        return Mono.just(accountService.consultBalanceOfPrincipalAccount(debitCardNumber));
    }

    @PostMapping("customers")
    public Mono<List<ResponseAccountDto>> findAccountByCustomersDnis(@RequestBody DocumentsDto customerDnis) throws Exception {
        return Mono.just(accountService.findAccountByCustomersDnis(customerDnis.getCustomerDnis()));
    }

}
