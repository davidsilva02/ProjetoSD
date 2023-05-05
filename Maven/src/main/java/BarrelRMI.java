import java.rmi.*;
import java.util.ArrayList;

public interface BarrelRMI extends Remote {
	public ArrayList<infoURL> resultsReferencesList(String url) throws RemoteException;
	public ArrayList<infoURL> resultadoPesquisa(String termo_pesquisa,Integer id_client) throws RemoteException;
	public ArrayList<infoURL> continueSearch(Integer id_client) throws RemoteException;
}