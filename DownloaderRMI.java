import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DownloaderRMI extends Remote {
    public void putUrlInQueue(String url) throws RemoteException;
}
