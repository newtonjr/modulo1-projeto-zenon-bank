package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TransactionSQLRepository implements TransactionRepository {
    public static final int JDBC_BATCH_SIZE = 1_000;

    public TransactionSQLRepository() {
    }

    @Override
    public void save(List<Transaction> itens) {
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

        int count = 0;

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                for (Transaction item : itens) {
                    createLinePS(item, ps);
                    ps.addBatch();
                    count++;

                    if (count % JDBC_BATCH_SIZE == 0) {
                        IO.println("Executando batch JDBC...");

                        ps.executeBatch();
                        conn.commit();
                    }
                }

                IO.println("Executando batch final JDBC...");

                int[] resultados = ps.executeBatch();

                conn.commit();
                conn.setAutoCommit(true);

                System.out.println("Batch executado! Total de linhas: " + resultados.length);

            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Erro ao executar rollback", ex);
                }
                throw new RuntimeException("Erro ao salvar nova transação ", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar as transações", e);
        }
    }

    private static void createLinePS(Transaction item, PreparedStatement ps) {
        try {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

            createLinePS(item, ps);

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
