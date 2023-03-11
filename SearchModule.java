import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class SearchModule extends UnicastRemoteObject implements RMI {

    private CopyOnWriteArrayList<Integer> barrels = new CopyOnWriteArrayList<>();
    private LinkedBlockingQueue<String> urlQueue = new LinkedBlockingQueue<>();



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

        boolean hasReceived = false;
        BarrelsRMI barrel=null;

        // loop through barrels while we can't get an answer
        while (!hasReceived){
            //get random barrel from the list
            int barrel_a_procurar = barrels.get(new Random().nextInt(barrels.size()));
            String address_barrel="rmi://localhost:"+ barrel_a_procurar+ "/barrel";

            try {
                barrel=(BarrelsRMI) Naming.lookup(address_barrel);
                hasReceived = true;
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NotBoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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

    @Override
    public String getUrl() throws RemoteException {
    
        
        String newUrl = null;

        while( newUrl == null ){
            try{
                newUrl = urlQueue.take();
            }catch(InterruptedException e){
                System.out.println("Exception taking an url from the queue: " +  e);;
            }
        }

        return newUrl;
    }

    @Override
    public void putUrl(String newUrl) throws RemoteException {

        try{
            urlQueue.add(newUrl);
        }catch(IllegalStateException e){
            System.out.println("Exception puting an url in the queue: " +  e);;
        }

    }
}
