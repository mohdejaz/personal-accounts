package home.ejaz.accounts;

import java.math.BigDecimal;
import java.util.Objects;

public class Budget extends DomainObject {
    private String name;

    private BigDecimal amt;

    private BigDecimal spend;

    public void setName(String name) {
        this.name = name;
    }

    public void setAmt(BigDecimal amt) {
        this.amt = amt;
    }

    public void setSpend(BigDecimal spend) {
        this.spend = spend;
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Budget))
            return false;
        Budget other = (Budget) o;
        if (!other.canEqual(this))
            return false;
        Object this$name = getName(), other$name = other.getName();
        if (!Objects.equals(this$name, other$name))
            return false;
        Object this$amt = getAmt(), other$amt = other.getAmt();
        if (!Objects.equals(this$amt, other$amt))
            return false;
        Object this$spend = getSpend(), other$spend = other.getSpend();
        return Objects.equals(this$spend, other$spend);
    }

    protected boolean canEqual(Object other) {
        return other instanceof Budget;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $name = getName();
        result = result * 59 + (($name == null) ? 43 : $name.hashCode());
        Object $amt = getAmt();
        result = result * 59 + (($amt == null) ? 43 : $amt.hashCode());
        Object $spend = getSpend();
        return result * 59 + (($spend == null) ? 43 : $spend.hashCode());
    }

    public String toString() {
        return "Budget(name=" + getName() + ", amt=" + String.valueOf(getAmt()) + ", spend=" + String.valueOf(getSpend()) + ")";
    }

    public String getName() {
        return this.name;
    }

    public BigDecimal getAmt() {
        return this.amt;
    }

    public BigDecimal getSpend() {
        return this.spend;
    }
}
