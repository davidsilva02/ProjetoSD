package com.sptest.sptest;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerRMI extends Remote {
    public String h() throws RemoteException;
}
