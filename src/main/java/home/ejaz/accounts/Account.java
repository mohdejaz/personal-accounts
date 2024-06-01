package home.ejaz.accounts;

import java.math.BigDecimal;
import java.util.Objects;

public class Account extends DomainObject {
    private String name;

    private BigDecimal balance;

    public void setName(String name) {
        this.name = name;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Account))
            return false;
        Account other = (Account) o;
        if (!other.canEqual(this))
            return false;
        Object this$name = getName(), other$name = other.getName();
        if (!Objects.equals(this$name, other$name))
            return false;
        Object this$balance = getBalance(), other$balance = other.getBalance();
        return Objects.equals(this$balance, other$balance);
    }

    protected boolean canEqual(Object other) {
        return other instanceof Account;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $name = getName();
        result = result * 59 + (($name == null) ? 43 : $name.hashCode());
        Object $balance = getBalance();
        return result * 59 + (($balance == null) ? 43 : $balance.hashCode());
    }

    public String toString() {
        return "Account(name=" + getName() + ", balance=" + String.valueOf(getBalance()) + ")";
    }

    public String getName() {
        return this.name;
    }

    public BigDecimal getBalance() {
        return this.balance;
    }
}
