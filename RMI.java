
import java.rmi.*;
import java.util.List;

public interface RMI extends Remote {

	public List<infoURL> resultadoPesquisa(String termo_pesquisa) throws java.rmi.RemoteException;

    // public void connectBarrel(int rmi_port) throws java.rmi.RemoteException;
    public void AvailableBarrel(BarrelRMI b) throws java.rmi.RemoteException;

    public void notAvailableBarrel(BarrelRMI b) throws java.rmi.RemoteException;

    public String getUrl() throws RemoteException;

    public void putUrl(String newUrl) throws RemoteException;
}