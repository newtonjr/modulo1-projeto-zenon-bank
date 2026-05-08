package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.util.List;

public class Main {
    static void main(String[] args) {
        Transaction transacao01 = new Transaction(
                1,
                TransactionType.PAYMENT,
                new BigDecimal("9839.64"),
                new TransactionCustomer("C1231006815", new BigDecimal("170136.0"), new BigDecimal("160296.36")),
                new TransactionCustomer("M1979787155", new BigDecimal("0.0"), new BigDecimal("0.0")),
                false,
                false);

        Transaction transacao02 = new Transaction(
                743,
                TransactionType.CASH_OUT,
                new BigDecimal("850002.52"),
                new TransactionCustomer("C1280323807", new BigDecimal("850002.52"), new BigDecimal("0.0")),
                new TransactionCustomer("C873221189", new BigDecimal("6510099.11"), new BigDecimal("7360101.63")),
                true,
                false);

        IO.println(transacao01);
        IO.println(transacao02);


        IO.println("------------------------------");

        TransactionIngestor transactionIngestor = new TransactionIngestor();
        List<Transaction> listTrans = transactionIngestor.readFile("data/PS_20174392719_1491204439457_log.csv");
        IO.println(listTrans.size());

        listTrans.stream().limit(10).forEach(IO::println);

        IO.println("------------------------------");

        List<Transaction> listTrans2 = transactionIngestor.readFile("data/paysim_with_bad_data.csv");
        IO.println(listTrans2.size());

        listTrans2.stream().forEach(IO::println);
    }
}
