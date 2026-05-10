package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class TransactionSQLRepository implements TransactionRepository {
    public TransactionSQLRepository() {
    }

    @Override
    public void save(Transaction item) {
        String sql = """
        insert into transactions (
            step, 
            `type`, 
            amount, 
            name_origin, 
            old_balance_origin,
            new_balance_origin,
            name_dest,
            old_balance_dest,
            new_balance_dest,
            is_fraud,
            is_flagged_fraud
        ) values (?,?,?,?,?,?,?,?,?,?,?);
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, item.step());
            ps.setString(2, item.type().name());
            ps.setBigDecimal(3, item.amount());

            ps.setString(4, item.orig().name());
            ps.setBigDecimal(5, item.orig().oldBalance());
            ps.setBigDecimal(6, item.orig().newBalance());

            ps.setString(7, item.dest().name());
            ps.setBigDecimal(8, item.dest().oldBalance());
            ps.setBigDecimal(9, item.dest().newBalance());

            ps.setBoolean(10, item.isFraud());
            ps.setBoolean(11, item.isFlaggedFraud());

            ps.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar nova transação: " + item, e);
        }
    }

    @Override
    public Optional<Transaction> getCustomerByName(String name) {

        String sql = """
            SELECT id,
            step, 
            `type`, 
            amount, 
            name_origin, 
            old_balance_origin,
            new_balance_origin,
            name_dest,
            old_balance_dest,
            new_balance_dest,
            is_fraud,
            is_flagged_fraud
            FROM zenon_frauds.transactions
            WHERE name_origin = ?
            ORDER BY step
            LIMIT 1
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);

            try(ResultSet rs = ps.executeQuery()) {

                if(rs.next()) {
                    IO.println(rs.getString("name_origin"));
                    Transaction transaction = mapResultSetToTransaction(rs);
                    return Optional.of(transaction);
                } else {
                    IO.println("Transacao nao encontrada para origin: " + name);
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar transação da origem: " + name, e);
        }
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) {
        try {
            int step = rs.getInt("step");
            TransactionType type = TransactionType.valueOf(rs.getString("type"));
            BigDecimal amount = rs.getBigDecimal("amount");

            String originName = rs.getString("name_origin");
            BigDecimal originOldBalance = rs.getBigDecimal("old_balance_origin");
            BigDecimal originNewBalance = rs.getBigDecimal("new_balance_origin");
            TransactionCustomer origin = new TransactionCustomer(originName, originOldBalance, originNewBalance);

            String destName = rs.getString("name_dest");
            BigDecimal destOldBalance = rs.getBigDecimal("old_balance_dest");
            BigDecimal destNewBalance = rs.getBigDecimal("new_balance_dest");
            TransactionCustomer dest = new TransactionCustomer(destName, destOldBalance, destNewBalance);

            boolean isFraud = rs.getBoolean("is_fraud");
            boolean isFlaggedFraud = rs.getBoolean("is_flagged_fraud");

            return new Transaction(step, type, amount, origin, dest, isFraud, isFlaggedFraud);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
