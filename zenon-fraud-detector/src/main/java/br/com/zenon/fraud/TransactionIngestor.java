package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TransactionIngestor {
    public List<Transaction> readFile(String filename) {
        Path path = Path.of(filename);
        try {
            List<String> lines = Files.readAllLines(path);
            return lines.stream().skip(1).limit(1000).map(this::parseLine).toList();
        } catch (Exception e) {
            throw new RuntimeException("Ocorreu um erro ao ler o arquivo: " + filename);
        }
    }

    private Transaction parseLine(String line) {
        String[] chunks = line.split(",");

        int step = Integer.parseInt(chunks[0]);
        TransactionType type = TransactionType.valueOf(chunks[1]);

        BigDecimal amount = new BigDecimal(chunks[2]);

        var orig = new TransactionCustomer(chunks[3], new BigDecimal(chunks[4]), new BigDecimal(chunks[5]));
        var dest = new TransactionCustomer(chunks[6], new BigDecimal(chunks[7]), new BigDecimal(chunks[8]));

        boolean isFraud = chunks[9].equals("1");

        boolean isFlaggedFraud = chunks[10].equals("1");

        return new Transaction(step, type, amount, orig, dest, isFraud, isFlaggedFraud);
    }
}
