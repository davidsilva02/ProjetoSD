import java.rmi.*;

public interface BarrelRMI extends Remote {

	public String resultadoPesquisa(String termo_pesquisa) throws java.rmi.RemoteException;    
}