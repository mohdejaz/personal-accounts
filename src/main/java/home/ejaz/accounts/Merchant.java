package home.ejaz.accounts;

import lombok.Data;

@Data
public class Merchant extends DomainObject {
    private String name;

    public Merchant(String name) {
        this.name = name;
    }
}
