package br.com.zenon.fraud;

import java.util.Optional;

public interface TransactionRepository {
    Optional<Transaction> getCustomerByName(String name);

    void save(Transaction item);
}
