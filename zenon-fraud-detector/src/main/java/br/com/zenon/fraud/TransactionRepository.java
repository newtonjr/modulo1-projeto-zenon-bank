package br.com.zenon.fraud;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    Optional<Transaction> getCustomerByName(String name);

    void save(Transaction item);

    void save(List<Transaction> itens);
}
