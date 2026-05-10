package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class EfficientTransactionIngestor {
    public static final Logger logger = Logger.getLogger(TransactionIngestor.class.getName());

    public static final int TOTAL_LIMIT = 10_000;
    public static final int LINE_BATCH_SIZE = 10_000;

    private final Semaphore dbPermits = new Semaphore(10);

    public void readAsBatch(String filename, Consumer<List<Transaction>> bacthConsumer) {
        Path path = Path.of(filename);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
             Stream<String> lines = Files.lines(path).skip(1)) {

//            int count = 0;
            var iterator = lines.iterator();
//            if (iterator.hasNext()) iterator.next();

            List<String> lineBatch = new ArrayList<>();
            while (iterator.hasNext()) {

                String line = iterator.next();
                lineBatch.add(line);
//                count++;
//
//                if (count >= TOTAL_LIMIT) {
//                    break;
//                }

                if (lineBatch.size() >= LINE_BATCH_SIZE) {
                    final List<String> currentLineBatch = List.copyOf(lineBatch);
                    executor.submit(() -> {
                        try {
                            executeBatch(currentLineBatch, bacthConsumer);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    lineBatch.clear();
                }
            }

            if (!lineBatch.isEmpty()) {
                IO.println("Executando ultimo batch");
                final List<String> currentLineBatch = List.copyOf(lineBatch);
                executor.submit(() -> {
                    try {
                        executeBatch(currentLineBatch, bacthConsumer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

        } catch (Exception e) {
            throw new RuntimeException("Ocorreu um erro ao ler o arquivo: " + filename);
        }
    }

    private void executeBatch(List<String> lineBatch, Consumer<List<Transaction>> bacthConsumer) {
        List<Transaction> transactions = lineBatch
                .stream().map(this::parseLine)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        try {
            dbPermits.acquire();
            try {
                bacthConsumer.accept(transactions);
            } finally {
                dbPermits.release();
            }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
    }

    public void readAsStream(String filename, Consumer<Transaction> consumer) {
        Path path = Path.of(filename);
        try (Stream<String> lines = Files.lines(path)) {
            lines.skip(1).limit(TOTAL_LIMIT).map(this::parseLine)
                    .filter(Optional::isPresent).map(Optional::get).forEach(consumer);
        } catch (Exception e) {
            throw new RuntimeException("Ocorreu um erro ao ler o arquivo: " + filename);
        }
    }

    private Optional<Transaction> parseLine(String line) {
        try {
            String[] chunks = line.split(",");

            int step = Integer.parseInt(chunks[0]);
            TransactionType type = TransactionType.valueOf(chunks[1]);

            BigDecimal amount = new BigDecimal(chunks[2]);

            var orig = new TransactionCustomer(chunks[3], new BigDecimal(chunks[4]), new BigDecimal(chunks[5]));
            var dest = new TransactionCustomer(chunks[6], new BigDecimal(chunks[7]), new BigDecimal(chunks[8]));

            boolean isFraud = chunks[9].equals("1");

            boolean isFlaggedFraud = chunks[10].equals("1");

            return Optional.of(new Transaction(step, type, amount, orig, dest, isFraud, isFlaggedFraud));
        } catch (Exception e) {
            // System.err.println("Erro ao fazer parse: " + line + " | " + e);
            logger.severe("Erro ao fazer parse: " + line + " | " + e);
            return Optional.empty();
        }
    }
}
