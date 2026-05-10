package br.com.zenon.fraud;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.ResourceBundle;

public class ReportMain {
    void main(String[] args) {
        // en-US | pt-BR
        String language = (args.length > 0 ? args[0]: "pt-BR");
        var locale = Locale.forLanguageTag(language);

        var integerFormatter = NumberFormat.getIntegerInstance(locale);
        var currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        currencyFormatter.setCurrency(Currency.getInstance(locale));

        // determina qual arquivo do idioma vai carregar do resorcou usando o locale
        var resourceBundle = ResourceBundle.getBundle("report", locale);

        var transactionReport = new TransactionReport();
        StatisticsTransaction statistics = transactionReport.generateReport("data/PS_20174392719_1491204439457_log.csv");

        // formata valores numéricos
        String fmtTotalTransactions = integerFormatter.format(statistics.totalTransactions());
        String fmtTotalFrauds = integerFormatter.format(statistics.totalFrauds());
        String fmtTotalAmount = currencyFormatter.format(statistics.totalAmount());

        // extrai os textos do resource
        String msgTotalTransactions = resourceBundle.getString("label.total.transactions");
        String msgTotalFrauds = resourceBundle.getString("label.total.frauds");
        String msgTotalAmount = resourceBundle.getString("label.total.amount");

        IO.println("""
        %s: %s
        %s: %s
        %s: %s
        """.formatted(
                msgTotalTransactions, fmtTotalTransactions,
                msgTotalFrauds, fmtTotalFrauds,
                msgTotalAmount, fmtTotalAmount
        ));
    }
}
