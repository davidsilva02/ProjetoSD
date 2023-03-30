
package org.ProjetoSD_MAVEN;

import java.rmi.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public interface RMI extends Remote {

    public ArrayList<Searched> getTopSearchs() throws RemoteException;

    public HashMap<String, Component> getComponents() throws RemoteException;

    public void makeDownloaderUnavailable(String threadName) throws RemoteException;

    public void makeDownloaderAvailable(String threadName) throws RemoteException;

    public void addDownloader(String threadName) throws RemoteException;

    public int makeRegister(String username, String pw) throws RemoteException;

    public String makeLogin(String username, String pw) throws RemoteException;

    public ArrayList<infoURL> resultadoPesquisa(String termo_pesquisa, Integer id_client)
            throws java.rmi.RemoteException;

    public ArrayList<infoURL> getReferencesList(String url) throws RemoteException;

    public void addBarrel(BarrelRMI b, String name) throws java.rmi.RemoteException;

    public void notAvailableBarrel(BarrelRMI b) throws java.rmi.RemoteException;

    public void makeBarrelUnavailable(String name) throws RemoteException;

    public void makeBarrelAvailable(String name) throws RemoteException;

    // public String getUrl() throws RemoteException;

    // public void putUrl(String newUrl) throws RemoteException;

    public void connectDwRMItoServer(DownloaderRMI ref) throws RemoteException;

    public void putURLClient(String newUrl) throws RemoteException;

    public Integer numberBarrels() throws RemoteException;

    public void updateBarrels(HashSet<Integer> hashs) throws RemoteException;

    public ArrayList<infoURL> continueSearching(Integer id_client, Integer hash_barrel) throws RemoteException;
}