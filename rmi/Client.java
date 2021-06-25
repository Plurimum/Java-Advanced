package rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Objects;

public final class Client {

    private Client() {}

    public static void main(final String... args) throws RemoteException {
        if (args == null || args.length != 5 || Arrays.stream(args).noneMatch(Objects::nonNull)) {
            System.out.println("Usage: Client name surname passportId accountId amount");
            return;
        }
        final Bank bank;
        try {
            bank = (Bank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }
        final String name = args[0];
        final String surname = args[1];
        final String passportId = args[2];
        final String accountId = args[3];
        final int amount;
        try {
            amount = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.out.println("Amount should be an integer number");
            return;
        }
        Person person = bank.getPerson(passportId, true);
        if (person == null) {
            person = bank.createPerson(passportId, name, surname);
            System.out.println("Person created");
        } else {
            System.out.println("Person already registered");
        }
        Account account = person.getAccount(accountId);
        if (account == null) {
            account = bank.createAccount(person.getPassport() + "#" + accountId);
            System.out.println("Person account created");
        } else {
            System.out.println("Person's account already registered");
        }
        System.out.println("Account id: " + account.getId());
        System.out.println("Money: " + account.getAmount());
        System.out.println("Adding money");
        account.setAmount(account.getAmount() + amount);
        System.out.println("Money: " + account.getAmount());
    }
}
