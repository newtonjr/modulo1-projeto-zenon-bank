package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.util.Objects;

public record Transaction(int step,
                          TransactionType type,
                          BigDecimal amount,
                          TransactionCustomer orig,
                          TransactionCustomer dest,
                          boolean isFraud,
                          boolean isFlaggedFraud) {
    public Transaction {
        Objects.requireNonNull(type);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(orig);
        Objects.requireNonNull(dest);

        if (step <= 0) throw new IllegalArgumentException("O valor de step precisa ser positivo: " + step);
        if (amount.signum() < 0) throw new IllegalArgumentException("O valor de amount não pode ser negativo: " + amount);
    }
}
