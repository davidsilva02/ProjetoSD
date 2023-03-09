
import java.rmi.*;

public interface RMI extends Remote {

	public String resultadoPesquisa(String termo_pesquisa) throws java.rmi.RemoteException;

    public void indexURL(String url) throws java.rmi.RemoteException;
    
}