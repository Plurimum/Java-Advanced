package rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account createAccount(final String id) throws RemoteException {
        final Account account = new RemoteAccount(id);
        if (accounts.putIfAbsent(id, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return getAccount(id);
        }
    }

    @Override
    public Account getAccount(final String id) {
        return accounts.get(id);
    }

    @Override
    public Person createPerson(String passport, String name, String surname) throws RemoteException {
        final Person person = new RemotePerson(passport, name, surname);
        if (persons.putIfAbsent(passport, person) == null) {
            UnicastRemoteObject.exportObject(person, port);
            return person;
        } else {
            return getPerson(passport, true);
        }
    }

    @Override
    public Person getPerson(String passport, boolean isRemote) throws RemoteException {
        final RemotePerson person = (RemotePerson) persons.get(passport);
        if (person == null) {
            return null;
        }
        if (isRemote) {
            return person;
        } else {
            Person localPerson = new LocalPerson(passport, person.getName(), person.getSurname());
            ConcurrentMap<String, Account> personAccounts = person.getAccounts();
            if (personAccounts != null) {
                personAccounts.keySet().forEach(subId -> {
                    try {
                        localPerson.setAccount(subId, new LocalAccount(localPerson.getPassport() + "#" + subId,
                                personAccounts.get(subId).getAmount()));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                });
            }
            return localPerson;
        }
    }

    @Override
    public Account createPersonAccount(String subId, Person person) throws RemoteException {
        final String id = person.getPassport() + "#" + subId;
        Account account = createAccount(id);
        person.setAccount(subId, account);
        return account;
    }
}
