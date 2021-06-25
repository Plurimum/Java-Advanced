package rmi;

public abstract class AbstractAccount implements Account {

    private final String id;
    private int amount;

    public AbstractAccount(final String id) {
        this.id = id;
        this.amount = 0;
    }

    public AbstractAccount(final String id, final int amount) {
        this.id = id;
        this.amount = amount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized int getAmount() {
        return amount;
    }

    @Override
    public synchronized void setAmount(final int amount) {
        this.amount = amount;
    }
}
