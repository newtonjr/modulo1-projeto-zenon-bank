package br.com.zenon.fraud;

import java.math.BigDecimal;

public record StatisticsTransaction(long totalTransactions, long totalFrauds, BigDecimal totalAmount) {
    public final static StatisticsTransaction ZERO = new StatisticsTransaction(0, 0, BigDecimal.ZERO);

    public StatisticsTransaction addReportTransaction(ReportTransaction rt) {
        return new StatisticsTransaction(
                totalTransactions + 1,
                totalFrauds + (rt.isFraud() ? 1 : 0),
                totalAmount.add(rt.amount()));
    }

    public StatisticsTransaction add(StatisticsTransaction other) {
        return new StatisticsTransaction(totalTransactions + other.totalTransactions,
                totalFrauds + other.totalFrauds,
                totalAmount.add(other.totalAmount));
    }
}
