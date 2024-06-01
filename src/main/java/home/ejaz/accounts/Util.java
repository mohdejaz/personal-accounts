package home.ejaz.accounts;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Util {
    private static Util util = new Util();

    public static Util getInstance() {
        return util;
    }

    public String getProperty(String name, String defVal) {
        String value = System.getProperty(name);
        if (value != null && !value.isEmpty()) //  && !value.isBlank()
            return value;
        return defVal;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                getProperty("jdbc.url", "jdbc:h2:tcp://localhost/accounts.db"),
                getProperty("jdbc.user", "sa"),
                getProperty("jdbc.password", "sa"));
    }

    public DomainObject getSelection(
            List<? extends DomainObject> choices,
            Extractor extractor,
            String pattern,
            String title) {
        if (choices.isEmpty()) return null;

        List<? extends DomainObject> choices2 = choices.stream()
                .filter(t -> extractor.extract(t).toLowerCase().contains(pattern.toLowerCase()))
                .collect(Collectors.toList());

        if (choices2.size() == 1) return choices2.get(0);

        for (int i = 0; i < choices2.size(); i++) {
            System.out.printf("%2d - %s\n", i, extractor.extract(choices2.get(i)));
        }

        System.out.print(title + ": ");
        System.out.flush();

        Scanner scanner = new Scanner(System.in);
        int index = scanner.nextInt();
        if (index >= 0 && index < choices2.size()) {
            return choices2.get(index);
        }

        return null;
    }
}
