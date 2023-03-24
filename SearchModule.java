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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;

public class SearchModule extends UnicastRemoteObject implements RMI {

    private CopyOnWriteArrayList<BarrelRMI> barrels=new CopyOnWriteArrayList<>();
    private BlockingDeque<String> urlQueue = new LinkedBlockingDeque<>();
    private HashMap<String,Integer> users = new HashMap<>();
    private HashMap<String,Component> system = new HashMap<>(); // IP:NAME, Boolean stands for isAvailable, (True/False)
    private ArrayList<Searched> searches = new ArrayList<>();

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
    
    public boolean containsTerm(String term){
        return searches.stream().anyMatch(o -> o.getTerm().equals(term));
    }

    @Override
    public List<infoURL> resultadoPesquisa(String termo_pesquisa) throws RemoteException {
        System.out.printf("Client pesquisou %s \n",termo_pesquisa);

        //add to the searched terms list
        Searched newTerm = new Searched(1,termo_pesquisa);

        if( searches.contains(newTerm) ){
            
            //get the current term and update the number of searches for the term
            Searched updatedTerm = searches.get(searches.indexOf(newTerm));
            updatedTerm.setNumSearches( updatedTerm.getNumSearches() + 1);
            searches.set(searches.indexOf(newTerm),updatedTerm);
        }
        else
            searches.add(newTerm);

        searches.sort(Comparator.comparing(Searched::getNumSearches));


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

            Boolean flag=false;
            BarrelRMI barrel_a_procurar =null;
            while(flag==false){
            try{
                barrel_a_procurar= barrels.get(new Random().nextInt(barrels.size()));
                flag=true;
            }
            catch(IllegalArgumentException e){
                
            }}
            
            try {
                result = barrel_a_procurar.resultadoPesquisa(termo_pesquisa);
                hasValid=true;
            } catch (RemoteException e) {

                try {
                    notAvailableBarrel(barrel_a_procurar);
                } catch (RemoteException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } // TROQUEI PARA SER MAIS GENERICO E TIRAR DOS COMPONENTES
                // barrels.remove(barrels.indexOf(barrel_a_procurar));

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

                try {
                    notAvailableBarrel(barrel_a_procurar);
                } catch (RemoteException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } // TROQUEI PARA SER MAIS GENERICO E TIRAR DOS COMPONENTES
                // barrels.remove(barrels.indexOf(barrel_a_procurar));
                
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
                Component newComp = new Component(getClientHost(),true);
                system.put(threadName,newComp);
                
                //DEBUG
                System.out.println("ADD DW " + getClientHost() + ":" + threadName);
            } catch (ServerNotActiveException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
  

        System.out.println("Downloader disponivel:" + threadName);
    }

    @Override
    public void makeDownloaderAvailable(String threadName) throws RemoteException {
        system.replace(threadName, new Component(system.get(threadName).getIp(), true));
    }

    @Override
    public void makeDownloaderUnavailable(String threadName) throws RemoteException {
        system.replace(threadName, new Component(system.get(threadName).getIp(), false));
    }

    @Override
    public void AvailableBarrel(BarrelRMI b) throws RemoteException {
        barrels.add(b);

        try {
            system.put(String.format("%s",b.hashCode()),new Component(getClientHost(), true));

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

        system.replace(Integer.toString(b.hashCode()), new Component( system.get(Integer.toString(b.hashCode())).getIp() , false) );
 

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
            if(!hashs.contains(b.hashCode())) notAvailableBarrel(b);;
        }
    }


    @Override
    public void putURLClient(String newUrl) throws RemoteException {

        //adicionar no inicio da lista
        urlQueue.addFirst(newUrl);
        
    }

    public HashMap<String,Component> getComponents() throws RemoteException {
        System.out.println("GET COMPONENTS");

        return this.system;
    }

    public ArrayList<Searched> getTopSearchs() throws RemoteException {
        System.out.println("GET TOP SEARCHS");

        if (searches.size() >= 10)
            return new ArrayList(searches.subList(0, 10));
        else{
            System.out.println("SMALL SIZE");
            return new ArrayList(searches.subList(0, searches.size()));

        }
    }
}
