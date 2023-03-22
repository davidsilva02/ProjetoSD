import java.rmi.*;
import java.util.List;

public interface BarrelRMI extends Remote {
	public List<infoURL> resultsReferencesList(String url) throws java.rmi.RemoteException;
	public List<infoURL> resultadoPesquisa(String termo_pesquisa) throws java.rmi.RemoteException;    
}