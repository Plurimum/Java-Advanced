package rmi;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentMap;

public interface Person extends Remote, Serializable {

    String getName() throws RemoteException;

    String getSurname() throws RemoteException;

    String getPassport() throws RemoteException;

    ConcurrentMap<String, Account> getAccounts() throws RemoteException;

    void setAccount(String subId, Account account) throws RemoteException;

    Account getAccount(String subId) throws RemoteException;
}
