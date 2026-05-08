package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FraudAnalyzer {
    private final List<Transaction> transactionsFrauds;

    public FraudAnalyzer(List<Transaction> transactions) {
        transactionsFrauds = transactions.stream().filter(Transaction::isFraud).toList();
    }

    public int size() {
        return transactionsFrauds.size();
    }

    public List<BigDecimal> findHighFraudsAmountsByLimit(int limit) {
        return highValues().map(Transaction::amount).limit(limit).toList();
    }

    public List<String> findTopCustomerSuspiciousByLimit(int limit) {
        return highValues()
                .map(transaction -> transaction.orig().name())
                .distinct().limit(limit).toList();
    }

    public BigDecimal calcTotalFraud() {
        return listFraud().map(Transaction::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<TransactionType, Long> countFraudsByType() {
        return listFraud().collect(Collectors.groupingBy(Transaction::type, Collectors.counting()));
    }

    private Stream<Transaction> highValues() {
        return listFraud().sorted(Comparator.comparing(Transaction::amount).reversed());
    }

    private Stream<Transaction> listFraud() {
        return transactionsFrauds.stream();
    }
}
