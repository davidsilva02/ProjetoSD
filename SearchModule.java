import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
// import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class SearchModule extends UnicastRemoteObject implements RMI {

    private CopyOnWriteArrayList<BarrelRMI> barrels=new CopyOnWriteArrayList<>();
    private BlockingQueue<String> urlQueue = new LinkedBlockingQueue<>();
    // private ConcurrentLinkedQueue;



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

        String result = pesquisa_barrel(termo_pesquisa);
        
        return result;
    }

    /**
     * 
     * @param termo_pesquisa
     * @return List<infoURL> if everything is OK, null if there was no result or no available barrels
     */
    public List<infoURL> pesquisa_barrel(String termo_pesquisa){
        List<infoURL> result=null;
        boolean hasValid=false;
        
        // loop through barrels while we can't get an answer
        while (!hasValid){
            //get random barrel from the list
            if(barrels.size()==0) {
                result=null;
            }

            BarrelRMI barrel_a_procurar = barrels.get(new Random().nextInt(barrels.size()));
            
            try {
                result = barrel_a_procurar.resultadoPesquisa(termo_pesquisa);
                hasValid=true;
            } catch (RemoteException e) {

                barrels.remove(barrels.indexOf(barrel_a_procurar));
                if(barrels.size()==0) {
                    result=null;
                }
                e.printStackTrace();
                }
            }
            return result;
    }


    @Override
    public String getUrl() throws RemoteException {
    
        
        String newUrl = null;

        while( newUrl == null ){
            try{
                newUrl = urlQueue.take();
                urlQueue.remove(newUrl);
            }catch(InterruptedException e){
                System.out.println("Exception taking an url from the queue: " +  e);
            }
        }

        return newUrl;
    }

    @Override
    public void putUrl(String newUrl) throws RemoteException {
        try{
            urlQueue.add(newUrl);
        }catch(IllegalStateException e){
            System.out.println("Exception puting an url in the queue: " +  e);
        }

    }


    @Override
    public void AvailableBarrel(BarrelRMI b) throws RemoteException {
        barrels.add(b);

        System.out.println("Barrel disponivel:" + b.hashCode());
    }


    @Override
    public void notAvailableBarrel(BarrelRMI b) throws RemoteException {
        barrels.remove(barrels.indexOf(b));

        System.out.println("Barrel indisponivel:" + b.hashCode());

    }
}
