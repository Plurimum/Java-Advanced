package rmi;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractPerson implements Person {

    private final String passport;
    private final String name;
    private final String surname;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();

    public AbstractPerson(String passport, String name, String surname) {
        this.passport = passport;
        this.name = name;
        this.surname = surname;
    }

    @Override
    public String getPassport() {
        return passport;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public Account getAccount(String subId) throws RemoteException {
        return accounts.get(subId);
    }

    @Override
    public void setAccount(String subId, Account account) throws RemoteException {
        accounts.putIfAbsent(subId, account);
    }

    @Override
    public ConcurrentMap<String, Account> getAccounts() {
        return accounts;
    }
}
