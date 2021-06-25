package rmi;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LocalPerson extends AbstractPerson implements Serializable {

    public LocalPerson(String passport, String name, String surname) {
        super(passport, name, surname);
    }
}
