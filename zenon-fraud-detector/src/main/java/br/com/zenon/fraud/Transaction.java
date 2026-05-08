package br.com.zenon.fraud;

import java.math.BigDecimal;

public record Transaction(int step,
                          TransactionType type,
                          BigDecimal amount,
                          TransactionCustomer orig,
                          TransactionCustomer dest,
                          boolean isFraud,
                          boolean isFlaggedFraud) {
}
