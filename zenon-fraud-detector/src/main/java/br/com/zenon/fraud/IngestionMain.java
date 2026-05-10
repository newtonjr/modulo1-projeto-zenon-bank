package br.com.zenon.fraud;

import java.util.List;

public class IngestionMain {
    void main() {
        ConnectionFactory.getConnection();
        IO.println("Conexão com o DB criada!");

        var repository = new TransactionSQLRepository();

        var efficientTransactionIngestor = new EfficientTransactionIngestor();

        IO.println("Iniciando adicao das transacoes no BD...");
        long startTimeSQL = System.nanoTime();

        efficientTransactionIngestor.readAsBatch("data/PS_20174392719_1491204439457_log.csv", repository::save);

        long endTimeSQL = System.nanoTime();
        IO.println("Tempo de inserção - SQL (ms): " + (endTimeSQL - startTimeSQL) / 1_000_000.0);

        repository.getCustomerByName("C1231006815")
                .ifPresentOrElse(IO::println, () -> IO.println("Transacao nao encontrada para: C1231006815"));
    }
}
