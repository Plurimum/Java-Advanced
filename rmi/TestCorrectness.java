package rmi;

import org.junit.After;
import org.junit.Test;
import org.junit.Before;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import static junit.framework.TestCase.*;

public class TestCorrectness {

    private static final int PORT = 8888;
    private static Bank bank;
    private static ProcessBuilder processBuilder;
    private static Process process;
    private final String passportId = "2281488";
    private final String name = "Fyodor";
    private final String surname = "Sumkin";
    private final String accountId = "1509";

    @Before
    public void init() throws IOException, NotBoundException, URISyntaxException {
        BankWebServer.main(new String[]{});
        process = null;
        processBuilder = new ProcessBuilder("rmiregistry");
        //processBuilder.environment().put("CLASSPATH", Path.of(BankWebServer.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString());
        process = processBuilder.start();
        Server.main(String.valueOf(PORT));
        bank = (Bank) Naming.lookup("//localhost/bank");
        System.out.println("Bank initialized");
    }

    @Test
    public void createPerson() throws RemoteException {
        assertNull(getDefaultPerson(true));
        createDefaultPerson();
        assertNotNull(getDefaultPerson(true));
    }

    @Test
    public void getRemotePerson() throws RemoteException {
        createDefaultPerson();
        Person remotePerson = getDefaultPerson(true);
        checkPerson(remotePerson, passportId, name, surname);
    }

    @Test
    public void getLocalPerson() throws RemoteException {
        createDefaultPerson();
        Person localPerson = getDefaultPerson(false);
        checkPerson(localPerson, passportId, name, surname);
    }

    @Test
    public void createManyPersons() throws RemoteException {
        for (int i = 0; i < 228; i++) {
            bank.createPerson(passportId + i, name + i, surname + i);
        }
        for (int i = 0; i < 228; i++) {
            String curPassport = passportId + i;
            String curName = name + i;
            String curSurname = surname + i;
            Person remotePerson = bank.getPerson(curPassport, true);
            Person localPerson = bank.getPerson(curPassport, false);
            checkPerson(remotePerson, curPassport, curName, curSurname);
            checkPerson(localPerson, curPassport, curName, curSurname);
        }
    }

    @Test
    public void createAccountRemote() throws RemoteException {
        createDefaultPerson();
        createAccount(true);
    }

    @Test
    public void createAccountLocal() throws RemoteException {
        createDefaultPerson();
        createAccount(false);
    }

    @Test
    public void createManyRemoteAccounts() throws RemoteException {
        createDefaultPerson();
        Person remotePerson = getDefaultPerson(true);
        Person localPerson = getDefaultPerson(false);
        for (int i = 0; i < 228; i++) {
            bank.createPersonAccount(accountId + i, remotePerson);
        }
        for (int i = 0; i < 228; i++) {
            Account account = remotePerson.getAccount(accountId + i);
            checkAccount(account, localPerson, i);
        }
    }

    @Test
    public void createManyLocalAccounts() throws RemoteException {
        createDefaultPerson();
        Person localPerson = getDefaultPerson(false);
        Person remotePerson = getDefaultPerson(true);
        for (int i = 0; i < 228; i++) {
            LocalAccount localAccount = new LocalAccount(passportId + "#" + accountId + i);
            localPerson.setAccount(accountId + i, localAccount);
            Account account = localPerson.getAccount(accountId + i);
            checkAccount(account, remotePerson, i);
        }
    }

    @Test
    public void localDataAfterSetAmountOnRemote() throws RemoteException {
        setAmountOnRemote(false);
    }

    @Test
    public void setAccountAmountRemotePersonBeforeLocalPerson() throws RemoteException {
        setAmountOnRemote(true);
    }

    @Test
    public void setAccountAmountLocalPerson() throws RemoteException {
        int amount = 500;
        createDefaultPerson();
        Person remotePerson = getDefaultPerson(true);
        bank.createPersonAccount(accountId, remotePerson);
        Person localPerson = getDefaultPerson(false);
        Account account = localPerson.getAccount(accountId);
        account.setAmount(amount);
        assertEquals(remotePerson.getAccount(accountId).getAmount(), 0);
        assertEquals(localPerson.getAccount(accountId).getAmount(), amount);
    }

    @After
    public void terminateBank() {
        process.destroy();
        process = null;
    }

    private void checkPerson(Person person, String expectedPassport, String expectedName, String expectedSurname)
            throws RemoteException{
        assertEquals(person.getPassport(), expectedPassport);
        assertEquals(person.getName(), expectedName);
        assertEquals(person.getSurname(), expectedSurname);
    }

    private void createDefaultPerson() throws RemoteException {
        bank.createPerson(passportId, name, surname);
    }

    private Person getDefaultPerson(boolean isRemote) throws RemoteException {
        return bank.getPerson(passportId, isRemote);
    }

    private void createAccount(boolean isRemote) throws RemoteException {
        Person person = bank.getPerson(passportId, isRemote);
        Account created;
        if (isRemote) {
            created = new RemoteAccount(passportId + "#" + accountId);
        } else {
            created = new LocalAccount(passportId + "#" + accountId);
        }
        person.setAccount(accountId, created);
        Account account = person.getAccount(accountId);
        assertEquals(account.getId(), passportId + "#" + accountId);
        assertEquals(account.getAmount(), 0);
    }


    private void checkAccount(Account account, Person person, int i) throws RemoteException{
        assertEquals(account.getId(), passportId + "#" + accountId + i);
        assertEquals(account.getAmount(), 0);
        account = person.getAccount(accountId + i);
        assertNull(account);
    }



    public void setAmountOnRemote(boolean beforeGetLocal) throws RemoteException{
        int amount = 500, expectedAmount;
        createDefaultPerson();
        Person remotePerson = getDefaultPerson(true);
        Account account = bank.createPersonAccount(accountId, remotePerson);
        Person localPerson;
        if (beforeGetLocal) {
            account.setAmount(amount);
            localPerson = getDefaultPerson(false);
            expectedAmount = amount;
        } else {
            localPerson = getDefaultPerson(false);
            account.setAmount(amount);
            expectedAmount = 0;
        }
        assertEquals(remotePerson.getAccount(accountId).getAmount(), amount);
        assertEquals(localPerson.getAccount(accountId).getAmount(), expectedAmount);
    }
}
