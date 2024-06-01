package home.ejaz.accounts;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;

public class Transaction extends DomainObject implements Comparator {
    private Long id;

    private String account;

    private String merchant;

    private String bucket;

    private Date txdate;

    private BigDecimal amt;

    private String note;

    private BigDecimal balance;

    public void setId(Long id) {
        this.id = id;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public void setTxdate(Date txdate) {
        this.txdate = txdate;
    }

    public void setAmt(BigDecimal amt) {
        this.amt = amt;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public int compare(Object o1, Object o2) {
        Transaction t1 = (Transaction)o1;
        Transaction t2 = (Transaction)o2;

        if (t1.id.longValue() == t2.id.longValue()) return 0;
        if (t1.id < t2.id) return -1;
        return 1;
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Transaction))
            return false;
        Transaction other = (Transaction)o;
        if (!other.canEqual(this))
            return false;
        Object this$id = getId(), other$id = other.getId();
        if ((this$id == null) ? (other$id != null) : !this$id.equals(other$id))
            return false;
        Object this$account = getAccount(), other$account = other.getAccount();
        if ((this$account == null) ? (other$account != null) : !this$account.equals(other$account))
            return false;
        Object this$merchant = getMerchant(), other$merchant = other.getMerchant();
        if ((this$merchant == null) ? (other$merchant != null) : !this$merchant.equals(other$merchant))
            return false;
        Object this$bucket = getBucket(), other$bucket = other.getBucket();
        if ((this$bucket == null) ? (other$bucket != null) : !this$bucket.equals(other$bucket))
            return false;
        Object this$txdate = getTxdate(), other$txdate = other.getTxdate();
        if ((this$txdate == null) ? (other$txdate != null) : !this$txdate.equals(other$txdate))
            return false;
        Object this$amt = getAmt(), other$amt = other.getAmt();
        if ((this$amt == null) ? (other$amt != null) : !this$amt.equals(other$amt))
            return false;
        Object this$note = getNote(), other$note = other.getNote();
        if ((this$note == null) ? (other$note != null) : !this$note.equals(other$note))
            return false;
        Object this$balance = getBalance(), other$balance = other.getBalance();
        return !((this$balance == null) ? (other$balance != null) : !this$balance.equals(other$balance));
    }

    protected boolean canEqual(Object other) {
        return other instanceof Transaction;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $id = getId();
        result = result * 59 + (($id == null) ? 43 : $id.hashCode());
        Object $account = getAccount();
        result = result * 59 + (($account == null) ? 43 : $account.hashCode());
        Object $merchant = getMerchant();
        result = result * 59 + (($merchant == null) ? 43 : $merchant.hashCode());
        Object $bucket = getBucket();
        result = result * 59 + (($bucket == null) ? 43 : $bucket.hashCode());
        Object $txdate = getTxdate();
        result = result * 59 + (($txdate == null) ? 43 : $txdate.hashCode());
        Object $amt = getAmt();
        result = result * 59 + (($amt == null) ? 43 : $amt.hashCode());
        Object $note = getNote();
        result = result * 59 + (($note == null) ? 43 : $note.hashCode());
        Object $balance = getBalance();
        return result * 59 + (($balance == null) ? 43 : $balance.hashCode());
    }

    public String toString() {
        return "Transaction(id=" + getId() + ", account=" + getAccount() + ", merchant=" + getMerchant() + ", bucket=" + getBucket() + ", txdate=" + String.valueOf(getTxdate()) + ", amt=" + String.valueOf(getAmt()) + ", note=" + getNote() + ", balance=" + String.valueOf(getBalance()) + ")";
    }

    public Long getId() {
        return this.id;
    }

    public String getAccount() {
        return this.account;
    }

    public String getMerchant() {
        return this.merchant;
    }

    public String getBucket() {
        return this.bucket;
    }

    public Date getTxdate() {
        return this.txdate;
    }

    public BigDecimal getAmt() {
        return this.amt;
    }

    public String getNote() {
        return this.note;
    }

    public BigDecimal getBalance() {
        return this.balance;
    }

    @Override
    public String getName() {
        return "T-" + id;
    }
}
