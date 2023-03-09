import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServer extends UnicastRemoteObject implements RMI {
    public RMIServer() throws RemoteException {
		super();
	}

    
    public static void main(String[] args) {
        System.out.println("SERVER A CORRER");

		try {
			RMIServer h = new RMIServer();
			LocateRegistry.createRegistry(1099).rebind("server", h);
			System.out.println("RMI SERVER ready.");
		} catch (RemoteException re) {
			System.out.println("Exception in RMISERVER.main: " + re);
		} /*catch (MalformedURLException e) {
			System.out.println("MalformedURLException in HelloImpl.main: " + e);
		}*/
    }


    @Override
    public String resultadoPesquisa(String termo_pesquisa) throws RemoteException {
        System.out.printf("Client pesquisou %s \n",termo_pesquisa);
        String result;

        //testar pesquisa, em contexto pratico teriamos de ir aos barrels (ficheiros objeto, acho)
        if(termo_pesquisa.equals("ABC")) result="COM RESULTADOS";
        else result="SEM RESULTADOS";

        return result;
    }


    @Override
    public void indexURL(String url) throws RemoteException {
        new Downloader(url);
    }
}
