package home.ejaz.accounts;

import java.util.Objects;

public class Bucket extends DomainObject {
    private String name;

    private String budget;

    public void setName(String name) {
        this.name = name;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Bucket))
            return false;
        Bucket other = (Bucket) o;
        if (!other.canEqual(this))
            return false;
        Object this$name = getName(), other$name = other.getName();
        if (!Objects.equals(this$name, other$name))
            return false;
        Object this$budget = getBudget(), other$budget = other.getBudget();
        return Objects.equals(this$budget, other$budget);
    }

    protected boolean canEqual(Object other) {
        return other instanceof Bucket;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $name = getName();
        result = result * 59 + (($name == null) ? 43 : $name.hashCode());
        Object $budget = getBudget();
        return result * 59 + (($budget == null) ? 43 : $budget.hashCode());
    }

    public String toString() {
        return "Bucket(name=" + getName() + ", budget=" + getBudget() + ")";
    }

    public String getName() {
        return this.name;
    }

    public String getBudget() {
        return this.budget;
    }
}
