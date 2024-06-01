package home.ejaz.accounts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.types.Commandline;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.OSUtils;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Command
public class Main {
    private final Logger logger = LogManager.getLogger(Main.class);

    private final DataAccessObject dao = new DataAccessObject();

    private final String DELIM = ",";

    private final DateTimeFormatter DTF1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final SimpleDateFormat SDF1 = new SimpleDateFormat("yyyy-MM-dd");

    private String join(String delim, Object... columns) {
        return Arrays.<Object>stream(columns).map(Object::toString).collect(Collectors.joining(delim));
    }

    @Command(name = "exit", description = {"Exit"}, mixinStandardHelpOptions = true, version = {"1.0"})
    public void exit() {
        System.exit(0);
    }

    @Command(name = "bkp", description = {"Backup"}, mixinStandardHelpOptions = true, version = {"1.0"})
    public void backup() {
        Connection conn = null;
        Statement pstmt = null;
        ResultSet rs = null;
        try {
            conn = Util.getInstance().getConnection();
            pstmt = conn.createStatement();
            pstmt.execute("script TO 'accounts.sql'");
            pstmt.execute("backup TO 'accounts.zip'");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (pstmt != null)
                    pstmt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Command(name = "list-merchants", aliases = {"lsm"}, description = {"List Merchants"})
    public void listMerchants(@Option(names = {"-p"}, description = {"Merchant name"}) String pattern) {
        List<Merchant> merchants = null;
        try {
            merchants = this.dao.getMerchants();
            if (pattern != null)
                merchants = merchants.stream().filter(m -> m.getName().toLowerCase().contains(pattern.toLowerCase()))
                        .collect(Collectors.toList());
            System.out.println("Merchant");
            merchants.forEach(mer -> System.out.println(mer));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Command(name = "list-buckets", aliases = {"lsb"}, description = {"List Buckets"})
    public void listBuckets(@Option(names = {"-p"}, description = {"Bucket name"}) String pattern) {
        List<Bucket> buckets;
        try {
            buckets = this.dao.getBuckets();
            if (pattern != null)
                buckets = buckets.stream().filter(a -> a.getName().toLowerCase().contains(pattern.toLowerCase())).collect(Collectors.toList());
            System.out.println("Bucket,Budget");
            buckets.forEach(buck -> System.out.println(join(",", buck.getName(), (buck.getBudget() == null) ? "?" : buck.getBudget())));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static class Exclusive {
        @Option(names = {"-ytd"}, description = {"yyyyMMdd - Year budget"}, required = true)
        Date ytd;

        @Option(names = {"-mtd"}, description = {"yyyyMMdd - Monthly budget"}, required = true)
        Date mtd;

        @Option(names = {"-list"}, description = {"List budget names"}, required = true)
        boolean show;
    }

    @Command(name = "budgets", aliases = {"bud"}, description = {"Budgets"})
    public void listBudgets(@ArgGroup(exclusive = true, multiplicity = "1") Exclusive exclusive) {
        if (exclusive.show) {
            List<Budget> budgets = null;
            try {
                budgets = this.dao.getBudgets();
                System.out.println(join(",", "Budget", "Amount"));
                budgets.forEach(budget -> System.out.println(join(",", budget.getName(), ((budget.getAmt() == null) ? BigDecimal.ZERO : budget.getAmt()).toString())));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (exclusive.ytd != null)
            listBudgetsYtd(exclusive.ytd);
        if (exclusive.mtd != null)
            listBudgetsMtd(exclusive.mtd);
    }

    private void listBudgetsYtd(Date ytd) {
        // throw new RuntimeException("Method not implemented!");
        Map<String, String> mapBuck2Budget = new HashMap<>();
        Map<String, Budget> mapBudgets = new HashMap<>();
        try {
            final int months = LocalDate.now().getMonthValue();
            LocalDate som = ytd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1).withMonth(1);
            String yyyy = DateTimeFormatter.ofPattern("yyyy").format(som);
            SimpleDateFormat SDF = new SimpleDateFormat("yyyy");
            List<Bucket> buckets = this.dao.getBuckets();
            buckets.forEach(bucket -> mapBuck2Budget.put(bucket.getName(), bucket.getBudget()));
            List<Budget> budgets = this.dao.getBudgets();
            budgets.forEach(budget -> {
                budget.setSpend(BigDecimal.ZERO);
                budget.setAmt(budget.getAmt().multiply(new BigDecimal(months)));
                mapBudgets.put(budget.getName(), budget);
            });
            List<Transaction> ytdTransactions = this.dao.getAllTransactions()
                    .stream()
                    .filter(tx -> SDF.format(tx.getTxdate()).equals(yyyy))
                    .collect(Collectors.toList());
            ytdTransactions.forEach(tx -> {
                String bucket = tx.getBucket();
                if (mapBuck2Budget.containsKey(bucket)) {
                    String budget = (String) mapBuck2Budget.get(bucket);
                    if (mapBudgets.containsKey(budget)) {
                        Budget budgetObj = (Budget) mapBudgets.get(bucket);
                        if (budgetObj != null)
                            budgetObj.setSpend(budgetObj.getSpend().add(tx.getAmt()));
                    }
                }
            });
            System.out.println(join(",", "Date", "Budget", "Amount", "Spend", "Diff"));
            budgets.forEach(budget -> System.out.println(join(",", som, budget.getName(), budget.getAmt(),
                    budget.getSpend(), budget.getAmt().subtract(budget.getSpend().abs()))));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void listBudgetsMtd(Date mtd) {
        Map<String, String> mapBuck2Budget = new HashMap<>();
        Map<String, Budget> mapBudgets = new HashMap<>();
        try {
            LocalDate som = mtd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1);
            String yyyyMM = DateTimeFormatter.ofPattern("yyyyMM").format(som);
            SimpleDateFormat SDF = new SimpleDateFormat("yyyyMM");
            List<Bucket> buckets = this.dao.getBuckets();
            buckets.forEach(bucket -> mapBuck2Budget.put(bucket.getName(), bucket.getBudget()));
            List<Budget> budgets = this.dao.getBudgets();
            budgets.forEach(budget -> {
                budget.setSpend(BigDecimal.ZERO);
                mapBudgets.put(budget.getName(), budget);
            });
            List<Transaction> ytdTransactions = this.dao.getAllTransactions().stream().filter(tx -> SDF.format(tx.getTxdate()).equals(yyyyMM)).collect(Collectors.toList());
            ytdTransactions.forEach(tx -> {
                String bucket = tx.getBucket();
                if (mapBuck2Budget.containsKey(bucket)) {
                    String budget = mapBuck2Budget.get(bucket);
                    if (mapBudgets.containsKey(budget)) {
                        Budget budgetObj = mapBudgets.get(bucket);
                        if (budgetObj != null)
                            budgetObj.setSpend(budgetObj.getSpend().add(tx.getAmt()));
                    }
                }
            });
            System.out.println(join(",", "Date", "Budget", "Amount", "Spend", "Diff"));
            budgets.forEach(budget -> System.out.println(join(",", som, budget.getName(), budget.getAmt(), budget.getSpend(), budget.getAmt().subtract(budget.getSpend().abs()))));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Command(name = "new-bucket", aliases = {"nwb"}, description = {"New Bucket"})
    public void newBucket(@Option(names = {"-n"}, description = {"Bucket name"}) String bucket) {
        List<Bucket> buckets = null;
        try {
            this.dao.newBucket(bucket);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Command(name = "list-accounts", aliases = {"lsa"}, description = {"List Accounts"})
    public void listAccounts(
            @Option(names = {"-p"},
                    description = {"Account name"}) String pattern,
            @Option(names = {"-b"}, description = {"Show balance"}) boolean balance) {
        List<Account> accounts;
        try {
            accounts = this.dao.getAccounts();
            accounts.forEach(acct -> acct.setBalance(BigDecimal.ZERO));
            if (pattern != null)
                accounts = accounts.stream().filter(a -> a.getName().toLowerCase().contains(pattern.toLowerCase())).collect(Collectors.toList());
            System.out.println(join(",", "Account", "Balance"));
            if (balance)
                accounts.parallelStream().forEach(acct -> {
                    try {
                        acct.setBalance(BigDecimal.ZERO);
                        List<Transaction> transactions = this.dao.getTransactions(acct.getName());
                        if (!transactions.isEmpty())
                            acct.setBalance(transactions.stream().map(Transaction::getAmt).reduce(BigDecimal.ZERO, BigDecimal::add));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            accounts.forEach(acct -> System.out.println(join(",", acct.getName(), acct.getBalance().toString())));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Command(name = "new-account", aliases = {"nwa"}, description = {"New Account"}, mixinStandardHelpOptions = true)
    public void newAccount(@Option(names = {"-n"}, description = {"Account name"}) String account) {
        try {
            this.dao.newAccount(account);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Command(name = "list-transactions", aliases = {"lst"}, description = {"List Transactions"})
    public void listTransactions(
            @Option(names = {"-a"}, description = {"Account"}, required = true) String account,
            @Option(names = {"-l"}, description = {"Limit"}, defaultValue = "10") int max) {
        if (account == null || account.isEmpty()) {
            System.err.println("No account set!");
            return;
        }
        try {
            List<Transaction> transactions = this.dao.getTransactions(account);
            if (transactions.size() > max && max > 0) {
                calcBalance(transactions);
                Collections.reverse(transactions);
                transactions = transactions.stream().limit(max).collect(Collectors.toList());
                System.out.println(join(",", new Object[]{"ID", "Account", "Merchant", "Bucket", "TxDate", "Amt", "Balance", "Note"}));
                transactions.forEach(tx -> System.out.println(join(",",
                        tx.getId().toString(),
                        tx.getAccount(),
                        tx.getMerchant(),
                        tx.getBucket(),
                        tx.getTxdate().toString(),
                        tx.getAmt().toString(),
                        tx.getBalance().toString(),
                        tx.getNote()))
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void calcBalance(List<Transaction> transactions) {
        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction tx : transactions) {
            balance = balance.add(tx.getAmt());
            tx.setBalance(balance);
        }
    }


    @Command(name = "new-transaction", aliases = {"nwt"}, description = {"New Bucket"})
    public void newTransaction(
            @Option(names = {"-a"}, description = {"Account"}, required = true) String acctName,
            @Option(names = {"-m"}, description = {"Merchant"}, required = true) String merchantName,
            @Option(names = {"-b"}, description = {"Bucket"}, required = true) String buckName,
            @Option(names = {"-d"}, description = {"Date"}, required = true) Date txdate,
            @Option(names = {"-t"}, description = {"Amount"}, required = true) BigDecimal amt,
            @Option(names = {"-n"}, description = {"Note"}) String note) {
        try {
            Util util = Util.getInstance();
            Account selectedAccount = (Account) util.getSelection(
                    this.dao.getAccounts(),
                    DomainObject::getName,
                    acctName,
                    "Account? ");
            if (selectedAccount == null) {
                System.err.println("Invalid account index!");
                return;
            }

            Merchant selectedMerchant = (Merchant) util.getSelection(
                    this.dao.getMerchants(),
                    DomainObject::getName,
                    merchantName,
                    "Merchant? ");
            if (selectedMerchant == null) {
                System.err.println("Invalid merchant index!");
                return;
            }

            Bucket selectedBucket = (Bucket) util.getSelection(
                    this.dao.getBuckets(),
                    DomainObject::getName,
                    buckName,
                    "Bucket? ");
            if (selectedBucket == null) {
                System.err.println("Invalid bucket index!");
                return;
            }

            this.dao.newTransaction(
                    selectedAccount.getName(),
                    selectedMerchant.getName(),
                    selectedBucket.getName(),
                    txdate,
                    amt,
                    note
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Command(name = "del-transaction", aliases = {"dlt"}, description = {"Delete Transaction"})
    public void delTransaction(@Option(names = {"-i"}, description = {"Id"}, required = true) long id) {
        try {
            this.dao.delTransaction(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Command(name = "cash-flow", aliases = {"cfl"}, description = {"Display cashflow"})
    public void cashFlow(@Option(names = {"-mtd"}, description = {"yyyyMMdd - Monthly cash flow"}, required = true) Date mtd) {
        try {
            LocalDate som = mtd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1);
            String yyyyMM = DateTimeFormatter.ofPattern("yyyyMM").format(som);
            SimpleDateFormat SDF = new SimpleDateFormat("yyyyMM");
            List<Transaction> ytdTransactions = this.dao.getAllTransactions().stream().filter(tx -> tx.getAccount().equals("Chase Check")).filter(tx -> SDF.format(tx.getTxdate()).equals(yyyyMM)).collect(Collectors.toList());
            BigDecimal balance = ytdTransactions.stream().map(Transaction::getAmt).reduce(BigDecimal.ZERO, BigDecimal::add);
            System.out.println(join(",", new Object[]{"Month", "Balance"}));
            System.out.println(join(",", new Object[]{this.SDF1.format(mtd), balance}));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() throws IOException {
        Terminal terminal = TerminalBuilder.terminal();
        try {
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
            if (OSUtils.IS_WINDOWS)
                reader.setVariable("blink-matching-paren", Integer.valueOf(0));
            while (true) {
                String cmd = reader.readLine("$ ");
                if (cmd != null && !cmd.trim().isEmpty()) {
                    CommandLine line = new CommandLine(this);
                    line.execute(Commandline.translateCommandline(cmd));
                }
                System.out.println();
            }
        } catch (Throwable throwable) {
            if (terminal != null)
                try {
                    terminal.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            throw throwable;
        }
    }

    public static void main(String[] args) throws IOException {
        (new Main()).run();
    }
}
