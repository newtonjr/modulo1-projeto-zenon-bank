package br.com.zenon.fraud;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TransactionMapRepository implements TransactionRepository {
    private final Map<String, Transaction> transactions;

    public TransactionMapRepository(List<Transaction> data) {
        transactions = data.stream().collect(Collectors.toMap(t -> t.orig().name(), Function.identity()));
    }

    @Override
    public Optional<Transaction> getCustomerByName(String name) {
        Optional<Transaction> result = transactions.containsKey(name) ? Optional.of(transactions.get(name)) : Optional.empty();
        if (result.isPresent()) {
            IO.println(result.get());
        } else {
            IO.println("Transação não encontrada para o cliente: " + name);
        }
        return result;
    }

    @Override
    public void save(Transaction item) {
        transactions.put(item.orig().name(), item);
    }

    @Override
    public void save(List<Transaction> itens) {
        transactions.putAll(itens.stream().collect(Collectors.toMap(t -> t.orig().name(), Function.identity())));
    }
}
