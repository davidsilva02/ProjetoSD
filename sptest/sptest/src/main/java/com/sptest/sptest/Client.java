package com.sptest.sptest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

@RestController
public class Client {
    ServerRMI r;
    protected Client() throws RemoteException {
        try {
            r = (ServerRMI) Naming.lookup("rmi://localhost:3366/server");
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("/hello")
    public String h () throws RemoteException {
        return r.h();
    }
}
