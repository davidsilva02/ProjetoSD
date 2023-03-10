import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class SearchModule extends UnicastRemoteObject implements RMI {

    private CopyOnWriteArrayList<Integer> barrels = new CopyOnWriteArrayList<>();




    public SearchModule() throws RemoteException {
		super();
	}

    
    public static void main(String[] args) {
        System.out.println("SERVER A CORRER");

		try {
			SearchModule h = new SearchModule();
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

        //testar com barrel 0 (depois tem de ser diferente)
        int barrel_a_procurar = barrels.get(0);

        String address_barrel="rmi://localhost:"+ barrel_a_procurar+ "/barrel";
        BarrelsRMI barrel=null;
        try {
            barrel=(BarrelsRMI) Naming.lookup(address_barrel);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NotBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return barrel.resultadoPesquisa(termo_pesquisa);
    }


    @Override
    public void indexURL(String url) throws RemoteException {
        new Downloader(url);
    }


    @Override
    public void connectBarrel(int rmi_port) throws RemoteException {
        barrels.add(rmi_port);

        System.out.printf("BARREL %d ESTA DISPONIVEL\n",rmi_port);
    }
    
    @Override
    public void notAvailableBarrel(int rmi_port) throws RemoteException {
        // TODO Auto-generated method stub

        //remove barrel
        barrels.remove(barrels.indexOf(rmi_port));
        System.out.printf("BARREL %d ESTA OCUPADO\n",rmi_port);
    }
}
