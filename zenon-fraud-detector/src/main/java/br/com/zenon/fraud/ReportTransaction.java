package br.com.zenon.fraud;

import java.math.BigDecimal;

public record ReportTransaction(BigDecimal amount, boolean isFraud) {
}
