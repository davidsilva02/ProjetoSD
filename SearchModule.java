import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
// import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class SearchModule extends UnicastRemoteObject implements RMI {

    private CopyOnWriteArrayList<BarrelRMI> barrels=new CopyOnWriteArrayList<>();
    private BlockingDeque<String> urlQueue = new LinkedBlockingDeque<>();
    private HashMap<String,Integer> users = new HashMap<>();
    private HashMap<String,Boolean> system = new HashMap<>(); // IP:NAME, Boolean stands for isActive, (True/False)

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
    public List<infoURL> resultadoPesquisa(String termo_pesquisa) throws RemoteException {
        System.out.printf("Client pesquisou %s \n",termo_pesquisa);

        List<infoURL> result = pesquisa_barrel(termo_pesquisa);
        
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

    public List<infoURL> getReferencesList(String url){
        	
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
                result = barrel_a_procurar.resultsReferencesList(url);
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
    public void addDownloader(String threadName) throws RemoteException {
        


            try {
                system.put(String.format("%s:%s",getClientHost(),threadName),false);
                
                //DEBUG
                System.out.println("ADD DW "+getClientHost());
            } catch (ServerNotActiveException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
  

        System.out.println("Downloader disponivel:" + threadName);
    }

    @Override
    public void makeDownloaderAvailable(String threadName) throws RemoteException {
        system.replace(threadName, false);
    }

    @Override
    public void makeDownloaderUnavailable(String threadName) throws RemoteException {
        system.replace(threadName, true);
    }

    @Override
    public void AvailableBarrel(BarrelRMI b) throws RemoteException {
        barrels.add(b);

        try {
            system.put(String.format("%s:%d",getClientHost(),b.hashCode()),false);

            //DEBUG
            System.out.println("ADD BARREL "+getClientHost());
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }

        System.out.println("Barrel disponivel:" + b.hashCode());
    }

    @Override
    public void notAvailableBarrel(BarrelRMI b) throws RemoteException {
        barrels.remove(barrels.indexOf(b));

        try {
            system.remove(String.format("%s:%d",getClientHost(),b.hashCode()));
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }

        System.out.println("Barrel indisponivel:" + b.hashCode());
    }


    @Override
    public String makeLogin(String username,String pw) throws RemoteException{
        int hashedRealPw = users.getOrDefault(username, -1);

        if( hashedRealPw != -1 && hashedRealPw == pw.hashCode())
            return username;

        return null;
    }
    
    @Override
    public int makeRegister(String username,String pw) throws RemoteException{
        
        // search for existing user
        if(  users.get(username) != null)
            return 1;
        
        users.put(username, pw.hashCode());
        return 0;
    }

    @Override
    public int numberBarrels() throws RemoteException {
        return barrels.size();
    }

    @Override
    public void updateBarrels(HashSet<Integer> hashs) throws RemoteException {
        for (BarrelRMI b: barrels){
            if(!hashs.contains(b.hashCode())) barrels.remove(b);
        }
    }


    @Override
    public void putURLClient(String newUrl) throws RemoteException {

        //adicionar no inicio da lista
        urlQueue.addFirst(newUrl);
        
    }
}
