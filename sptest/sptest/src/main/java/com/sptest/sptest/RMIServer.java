package com.sptest.sptest;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServer extends UnicastRemoteObject implements ServerRMI{

    protected RMIServer() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws RemoteException {
        RMIServer r = null;
        try {
            r=new RMIServer();
            LocateRegistry.createRegistry(3366).rebind("server", r);
            System.out.println("RMI SERVER ready.");
        } catch (RemoteException re) {
            System.out.println("Exception in RMISERVER.main: " + re);
            System.exit(1);
        }
    }

    @Override
    public String h() throws RemoteException {
        return "OLÁ DO SERVER";
    }
}