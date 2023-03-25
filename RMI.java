
import java.rmi.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

public interface RMI extends Remote {

    public ArrayList<Searched> getTopSearchs() throws RemoteException;

    public HashMap<String,Component> getComponents() throws RemoteException;

    public void makeDownloaderUnavailable(String threadName) throws RemoteException;

    public void makeDownloaderAvailable(String threadName) throws RemoteException;

    public void addDownloader(String threadName) throws RemoteException;

    public int makeRegister(String username,String pw) throws RemoteException;

    public String makeLogin(String username,String pw) throws RemoteException;

	public ArrayList<infoURL> resultadoPesquisa(String termo_pesquisa) throws java.rmi.RemoteException;

    public List<infoURL> getReferencesList(String url) throws RemoteException;
    
    public void AvailableBarrel(BarrelRMI b) throws java.rmi.RemoteException;

    public void notAvailableBarrel(BarrelRMI b) throws java.rmi.RemoteException;

    public String getUrl() throws RemoteException;

    public void putUrl(String newUrl) throws RemoteException;

    public void putURLClient(String newUrl) throws RemoteException;

    public int numberBarrels() throws RemoteException;
    
    public void updateBarrels(HashSet<Integer> hashs) throws RemoteException;
}