package br.com.zenon.fraud;

import java.util.List;
import java.util.Optional;

public class TransactionListRepository implements TransactionRepository {
    private final List<Transaction> transactions;

    public TransactionListRepository(List<Transaction> data) {
        transactions = data;
    }

    @Override
    public Optional<Transaction> getCustomerByName(String name) {
        Optional<Transaction> result = transactions.stream().filter(transaction -> transaction.orig().name().equals(name)).findFirst();
        if (result.isPresent()) {
            IO.println(result.get());
        } else {
            IO.println("Transação não encontrada para o cliente: " + name);
        }
        return result;
    }

    @Override
    public void save(Transaction item) {
        transactions.add(item);
    }
}
