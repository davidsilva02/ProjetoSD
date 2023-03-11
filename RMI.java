
import java.rmi.*;

public interface RMI extends Remote {

	public String resultadoPesquisa(String termo_pesquisa) throws java.rmi.RemoteException;

    public void indexURL(String url) throws java.rmi.RemoteException;


    public void connectBarrel(int rmi_port) throws java.rmi.RemoteException;

    public void notAvailableBarrel(int rmi_port) throws java.rmi.RemoteException;

    public String getUrl() throws RemoteException;

    public void putUrl(String newUrl) throws RemoteException;
}