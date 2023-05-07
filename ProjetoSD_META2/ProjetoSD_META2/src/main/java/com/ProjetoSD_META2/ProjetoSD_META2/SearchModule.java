package com.ProjetoSD_META2.ProjetoSD_META2;

import com.ProjetoSD_META2.ProjetoSD_META2.Spring.WebSocketClient;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
/**
 * Programa que serve de ponte para todos os componentes, é aqui que todos os componentes conectam-se para 
 * guardar o seu status, também é aqui que são guardados os termos mais pesquisados
 */
public class SearchModule extends UnicastRemoteObject implements RMI {

    private CopyOnWriteArrayList<BarrelRMI> barrels = new CopyOnWriteArrayList<>();
    private static HashMap<String, Integer> users;
    private HashMap<String, Component> system = new HashMap<>();
    private static CopyOnWriteArrayList<Searched> searches;
    DownloaderRMI dwRMI;

    ReentrantLock lockFile1;
    ReentrantLock lockFile2;

    public SearchModule() throws RemoteException {
        super();

        File folder = new File("./SM");
        folder.mkdir();

        File usrBin = new File("./SM/users.bin");

        if (!usrBin.exists()) {
            users = new HashMap<>();

            try {
                usrBin.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            users = (HashMap<String, Integer>) FileOps.readFromDisk(usrBin);

            if (users == null)
                users = new HashMap<>();

            // for( String key : users.keySet())
            // System.out.println(String.format("%s:%s",key,users.get(key)));
        }

        File searchBin = new File("./SM/searches.bin");

        if (!searchBin.exists()) {
            searches = new CopyOnWriteArrayList<Searched>();

            try {
                searchBin.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            searches = (CopyOnWriteArrayList<Searched>) FileOps.readFromDisk(searchBin);

            if (searches == null)
                searches = new CopyOnWriteArrayList<Searched>();

        }

        lockFile1 = new ReentrantLock();
        lockFile2 = new ReentrantLock();

    }

    public static void main(String[] args) {
        System.out.println("SERVER A CORRER");
        
        // Open server RMI
        SearchModule h = null;
        try {
            h = new SearchModule();
            LocateRegistry.createRegistry(3366).rebind("server", h);
            System.out.println("RMI SERVER ready.");
        } catch (RemoteException re) {
            System.out.println("Exception in RMISERVER.main: " + re);
            System.exit(1);
        } 

    }

    @Override
    public void connectDwRMItoServer(DownloaderRMI ref) throws RemoteException {
        //marcar como false os outros downloaders
        for (String s:system.keySet()){
            if(s.contains("Downloader")){
                system.remove(s);
            }
        }

        new WebSocketClient().sendMessage(system,new ArrayList<>(searches));

        // Get reference for the Downloader RMI
        this.dwRMI = ref;
    }

    public boolean containsTerm(String term) {
        return searches.stream().anyMatch(o -> o.getTerm().equals(term));
    }

    @Override
    synchronized public ArrayList<infoURL> resultadoPesquisa(String termo_pesquisa, Integer id_client)
            throws RemoteException {
        System.out.printf("Client pesquisou %s \n", termo_pesquisa);

        // add to the searched terms list
        Searched newTerm = new Searched(1, termo_pesquisa);

        if (searches.contains(newTerm)) {

            // get the current term and update the number of searches for the term
            Searched updatedTerm = searches.get(searches.indexOf(newTerm));
            updatedTerm.setNumSearches(updatedTerm.getNumSearches() + 1);
            searches.set(searches.indexOf(newTerm), updatedTerm);
        } else
            searches.add(newTerm);

        searches.sort(Comparator.comparing(Searched::getNumSearches).reversed());
        List<Searched> topSearches = null;
        try{
            topSearches=searches.subList(0,10);
        }
        catch (IndexOutOfBoundsException e){
            topSearches=searches;
        }

        System.out.println(topSearches);

        new WebSocketClient().sendMessage(system, topSearches);

        new Thread(() -> {

            lockFile1.lock();
            FileOps.writeToDisk(new File("./SM/searches.bin"), searches);
            lockFile1.unlock();
        }).start();

        if (barrels.size() == 0)
            return null;
        ArrayList<infoURL> result = pesquisa_barrel(termo_pesquisa, id_client);

        return (ArrayList<infoURL>) result;
    }

    /**
     * 
     * @param termo_pesquisa
     * @return List<infoURL> if everything is OK, null if there was no result or no
     *         available barrels
     */
    synchronized public ArrayList<infoURL> pesquisa_barrel(String termo_pesquisa, Integer id_client) {
        ArrayList<infoURL> result = null;
        boolean hasValid = false;
        BarrelRMI barrel_a_procurar = null;

        // loop through barrels while we can't get an answer
        while (!hasValid) {
            // get random barrel from the list
            if (barrels.size() == 0) {
                result = null;
            }
            barrel_a_procurar = null;
            Boolean flag = false;
            while (flag == false) {
                try {
                    barrel_a_procurar = barrels.get(new Random().nextInt(barrels.size()));
                    flag = true;
                } catch (IllegalArgumentException e) {

                }
            }

            try {
                result = barrel_a_procurar.resultadoPesquisa(termo_pesquisa, id_client);
                hasValid = true;
            } catch (RemoteException e) {

                try {
                    notAvailableBarrel(barrel_a_procurar);
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }

                if (barrels.size() == 0) {
                    result = null;
                }
                e.printStackTrace();
            }
        }
        // System.out.println(result);
        // System.out.println(result.size());
        if (result != null && result.get(result.size() - 1).getUrl().equals("fim"))
            return result;
        else if (result != null)
            result.add(new infoURL(Integer.toString(barrel_a_procurar.hashCode())));
        return result;
    }

    public ArrayList<infoURL> getReferencesList(String url) {

        ArrayList<infoURL> result = null;
        boolean hasValid = false;

        // loop through barrels while we can't get an answer
        while (!hasValid) {
            // get random barrel from the list
            if (barrels.size() == 0) {
                result = null;
            }

            BarrelRMI barrel_a_procurar = barrels.get(new Random().nextInt(barrels.size()));

            try {
                result = barrel_a_procurar.resultsReferencesList(url);
                hasValid = true;
            } catch (RemoteException e) {

                try {
                    notAvailableBarrel(barrel_a_procurar);
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                } // TROQUEI PARA SER MAIS GENERICO E TIRAR DOS COMPONENTES
                  // barrels.remove(barrels.indexOf(barrel_a_procurar));

                if (barrels.size() == 0) {
                    result = null;
                }
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    public void addDownloader(String threadName) throws RemoteException {

        try {

            Component newComp = new Component(getClientHost(), true);
            system.put(threadName, newComp);

            // DEBUG
            System.out.println("ADD DW " + getClientHost() + ":" + threadName);
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }

        new WebSocketClient().sendMessage(system,new ArrayList<>(searches));

        System.out.println("Downloader disponivel:" + threadName);
    }

    @Override
    public void makeDownloaderAvailable(String threadName) throws RemoteException {
        system.replace(threadName, new Component(system.get(threadName).getIp(), true));
        new WebSocketClient().sendMessage(system,new ArrayList<>(searches));

    }

    @Override
    public void makeDownloaderUnavailable(String threadName) throws RemoteException {
        system.replace(threadName, new Component(system.get(threadName).getIp(), false));
        new WebSocketClient().sendMessage(system,new ArrayList<>(searches));

    }

    @Override
    public void addBarrel(BarrelRMI b, String name) throws RemoteException {
        barrels.add(b);

        try {
            system.put(name, new Component(getClientHost(), true));

            // DEBUG
            System.out.println("ADD BARREL " + getClientHost() + ":" + name);
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }

        // adicionar barrel no Downloader
        if (dwRMI != null) {
            try {
                dwRMI.updateNumberBarrels(barrels.size());
            } catch (RemoteException e1) {
                System.out.println("Ainda nao existe um downloader");
            }
        }
        new WebSocketClient().sendMessage(system,new ArrayList<>(searches));

        System.out.println("Barrel disponivel:" + b.hashCode());
    }

    public void makeBarrelUnavailable(String name) throws RemoteException {
        system.replace(name, new Component(system.get(name).getIp(), false));
        new WebSocketClient().sendMessage(system,new ArrayList<>(searches));

    }

    public void makeBarrelAvailable(String name) throws RemoteException {
        system.replace(name, new Component(system.get(name).getIp(), true));
        new WebSocketClient().sendMessage(system,new ArrayList<>(searches));

    }

    @Override
    public void notAvailableBarrel(BarrelRMI b) throws RemoteException {
        barrels.remove(barrels.indexOf(b));

        // system.replace(Integer.toString(b.hashCode()), new Component(
        // system.get(Integer.toString(b.hashCode())).getIp() , false) );

        // adicionar barrel no Downloader
        if (dwRMI != null) {
            try {
                dwRMI.updateNumberBarrels(barrels.size());
            } catch (RemoteException e1) {
                System.out.println("Ainda nao existe um downloader");
            }
        }
        new WebSocketClient().sendMessage(system,new ArrayList<>(searches));

        System.out.println("Barrel indisponivel:" + b.hashCode());
    }

    @Override
    public String makeLogin(String username, String pw) throws RemoteException {
        int hashedRealPw = users.getOrDefault(username, -1);

        if (hashedRealPw != -1 && hashedRealPw == pw.hashCode())
            return username;

        return null;
    }

    @Override
    public int makeRegister(String username, String pw) throws RemoteException {

        // search for existing user
        if (users.get(username) != null)
            return 1;

        users.put(username, pw.hashCode());
        new Thread(() -> {
            lockFile2.lock();
            FileOps.writeToDisk(new File("./SM/users.bin"), users);
            lockFile2.unlock();
        }).start();

        return 0;
    }

    @Override
    public Integer numberBarrels() throws RemoteException {
        return barrels.size();
    }

    @Override
    public void updateBarrels(HashSet<Integer> hashs) throws RemoteException {
        for (BarrelRMI b : barrels) {
            if (!hashs.contains(b.hashCode()))
                notAvailableBarrel(b);
            ;
        }
    }

    @Override
    public void putURLClient(String newUrl) throws RemoteException {

        Boolean flagWorked = false;

        System.out.println("A indexar o url: "+ newUrl);

        while (!flagWorked) {
            try {
                dwRMI.putUrlInQueue(newUrl);
                flagWorked = true;
            } catch (RemoteException e) {
                System.out.println("Ainda nao existe um downloader");
            }
        }

    }

    public HashMap<String, Component> getComponents() throws RemoteException {
        System.out.println("GET COMPONENTS");

        return this.system;
    }

    @Override
    synchronized public ArrayList<infoURL> continueSearching(Integer id_client, Integer hash_barrel)
            throws RemoteException {
        return cSearching(id_client, hash_barrel);
    }

    synchronized public ArrayList<infoURL> cSearching(Integer id_client, Integer hash_barrel) {

        ArrayList<infoURL> result = null;
        BarrelRMI barrel_a_procurar = null;

        for (BarrelRMI b : barrels) {
            if (b.hashCode() == hash_barrel) {
                barrel_a_procurar = b;
                break;
            }
        }

        if (barrels.size() == 0) {
            result = null;
        }
        try {
            result = barrel_a_procurar.continueSearch(id_client);
        } catch (RemoteException e) {
        }

        return result;
    }

    synchronized public ArrayList<Searched> getTopSearchs() throws RemoteException {
        System.out.println("GET TOP SEARCHS");

        if (searches.size() >= 10)
            return new ArrayList<Searched>(searches.subList(0, 10));
        else {
            return new ArrayList<Searched>(searches.subList(0, searches.size()));
        }
    }

}
