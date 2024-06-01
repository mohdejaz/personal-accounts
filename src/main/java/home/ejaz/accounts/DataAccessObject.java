package home.ejaz.accounts;

import sun.rmi.rmic.Main;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class DataAccessObject {
    public List<Merchant> getMerchants() throws SQLException {
        List<Merchant> merchants = new ArrayList<>();
        try (Connection conn = Util.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement("select * from MERCHANTS order by NAME");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                merchants.add(new Merchant(rs.getString("name")));
            }
        }
        return merchants;
    }

    public List<Account> getAccounts() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = Util.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement("select * from ACCOUNTS order by NAME");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Account account = new Account();
                account.setName(rs.getString("name"));
                accounts.add(account);
            }
        }
        return accounts;
    }

    public void newAccount(String account) throws SQLException {
        try (Connection conn = Util.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement("insert into ACCOUNTS(name, balance) values (?,?)")) {
            pstmt.setString(1, account);
            pstmt.setBigDecimal(2, BigDecimal.ZERO);
            pstmt.executeUpdate();
        }
    }

    public List<Bucket> getBuckets() throws SQLException {
        List<Bucket> buckets = new ArrayList<>();
        try (Connection conn = Util.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT b.NAME bucket, d.NAME budget" +
                             " FROM BUCKETS b" +
                             " LEFT OUTER JOIN BUDGET_BUCKETS bb ON b.ID = bb.BUCK_ID" +
                             " LEFT OUTER JOIN BUDGETS d ON d.ID = bb.BDGT_ID" +
                             " ORDER BY b.NAME");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Bucket bucket = new Bucket();
                bucket.setName(rs.getString("bucket"));
                bucket.setBudget(rs.getString("budget"));
                buckets.add(bucket);
            }
        }

        return buckets;
    }

    public List<Budget> getBudgets() throws SQLException {
        List<Budget> budgets = new ArrayList<>();
        try (Connection conn = Util.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Budgets ORDER BY NAME");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Budget budget = new Budget();
                budget.setName(rs.getString("name"));
                budget.setAmt(rs.getBigDecimal("amt"));
                budgets.add(budget);
            }
        }
        return budgets;
    }

    public void newBucket(String bucket) throws SQLException {
        try (Connection conn = Util.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement("insert into BUCKETS(name) values (?)")) {
            pstmt.setString(1, bucket);
            pstmt.executeUpdate();
        }
    }

    public List<Transaction> getTransactions(String account) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = Util.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT a.NAME account, t.* FROM TRANSACTIONS t, ACCOUNTS a" +
                             " WHERE a.id = t.ACCT_ID and a.name = ?" +
                             " ORDER BY t.txdate, t.id")) {
            pstmt.setString(1, account);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
                transactions.add(rs2trans(rs));
        }
        return transactions;
    }

    public List<Transaction> getAllTransactions() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = Util.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT a.NAME account, t.* FROM TRANSACTIONS t, ACCOUNTS a WHERE a.id = t.ACCT_ID ORDER BY t.txdate, t.id");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next())
                transactions.add(rs2trans(rs));
        }
        return transactions;
    }

    private Transaction rs2trans(ResultSet rs) throws SQLException {
        Transaction tx = new Transaction();
        tx.setId(rs.getLong("id"));
        tx.setAccount(rs.getString("account"));
        tx.setMerchant(rs.getString("merchant"));
        tx.setBucket(rs.getString("bucket"));
        tx.setTxdate(rs.getDate("txdate"));
        tx.setAmt(rs.getBigDecimal("amt"));
        tx.setNote(rs.getString("note"));
        return tx;
    }

    public void newTransaction(String account, String merchant, String bucket, Date txdate, BigDecimal amt, String note)
            throws SQLException {
        try (Connection conn = Util.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "insert into TRANSACTIONS(merchant, bucket, txdate, amt, note, acct_id)" +
                             " values (?, ?, ?, ?, ?, select id from ACCOUNTS where name = ?)")) {
            pstmt.setString(1, merchant);
            pstmt.setString(2, bucket);
            pstmt.setDate(3, new java.sql.Date(txdate.getTime()));
            pstmt.setBigDecimal(4, amt);
            pstmt.setString(5, (note == null) ? "NA" : note);
            pstmt.setString(6, account);
            pstmt.executeUpdate();
        }
    }

    public void delTransaction(long id) throws SQLException {
        try (Connection conn = Util.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement("delete from TRANSACTIONS where id = ?")) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }
}
