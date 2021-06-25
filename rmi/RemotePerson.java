package rmi;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemotePerson extends AbstractPerson {

    public RemotePerson(String passport, String name, String surname) {
        super(passport, name, surname);
    }
}
