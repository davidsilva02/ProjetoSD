package com.ProjetoSD_META2.ProjetoSD_META2;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DownloaderRMI extends Remote {
    public void putUrlInQueue(String url) throws RemoteException;
    public void updateNumberBarrels(Integer n) throws RemoteException;
}
