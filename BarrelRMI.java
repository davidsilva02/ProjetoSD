import java.rmi.*;
import java.util.ArrayList;
import java.util.List;

public interface BarrelRMI extends Remote {
	public ArrayList<infoURL> resultsReferencesList(String url) throws java.rmi.RemoteException;
	public ArrayList<infoURL> resultadoPesquisa(String termo_pesquisa) throws java.rmi.RemoteException;    
}