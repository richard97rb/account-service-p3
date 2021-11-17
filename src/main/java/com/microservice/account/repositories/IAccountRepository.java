package com.microservice.account.repositories;

import com.microservice.account.entities.Account;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IAccountRepository extends MongoRepository<Account, ObjectId> {
    Optional<Account> findAccountByAccountNumber(String accountNumber);
    List<Account> findAccountByCustomersIdsIn(List<ObjectId> customersIds);
}
