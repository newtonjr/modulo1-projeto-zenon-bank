package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public class TransactionReport {
    public StatisticsTransaction generateReport(String filename) {
        Path path = Path.of(filename);
        try (Stream<String> lines = Files.lines(path)) {
            return lines
                    .skip(1)
                    .map(this::parseReportTransaction)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .reduce(StatisticsTransaction.ZERO, StatisticsTransaction::addReportTransaction, StatisticsTransaction::add);
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao ler o arquivo: " + filename, ex);
        }
    }

    private Optional<ReportTransaction> parseReportTransaction(String line) {
        try {
            String[] chunks = line.split(",");

            if (chunks[2] == null || chunks[2].trim().isEmpty()) throw new IllegalArgumentException("O valor de amount nao pode ser nulo nem vazio");
            BigDecimal amount = new BigDecimal(chunks[2]);

            boolean isFraud = "1".equals(chunks[9]);

            return Optional.of(new ReportTransaction(amount, isFraud));
        } catch (Exception e) {
            System.err.println("Erro ao fazer parse: " + line + " | " + e);
            return Optional.empty();
        }
    }
}
