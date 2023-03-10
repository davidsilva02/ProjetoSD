import java.rmi.*;

public interface BarrelsRMI extends Remote {

	public String resultadoPesquisa(String termo_pesquisa) throws java.rmi.RemoteException;    
}