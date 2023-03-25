import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import org.jsoup.Jsoup;
import org.w3c.dom.views.DocumentView;

public class Downloader extends UnicastRemoteObject implements DownloaderRMI{
    
    RMI server;
    String name;
    BlockingQueue<JSOUPData> l;
    private BlockingDeque<String> urlQueue;

    //urls visitados
    Set <String> visited_urls;



    public Downloader() throws RemoteException{

        // Get the reference to the server to future RMI calls
        try {
            this.server= (RMI) Naming.lookup("rmi://localhost:1099/server");
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(0);
        }

        //não existe um concurrent HashMap, entao temos de transformar isto num set
        this.visited_urls = ConcurrentHashMap.newKeySet();
        this.l = new LinkedBlockingQueue<>();
        this.urlQueue = new LinkedBlockingDeque<>();

    }
    public static void main(String[] args){

        // Create object
        Downloader dw = null;
        try {
            dw = new Downloader();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        dw.start();

    }
    private void start() {

        // send reference to the server
        try {
            server.connectDwRMItoServer( (DownloaderRMI) this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        new MulticastSender("MS", this.l);


        //TODO: Colocar por argumento o numero de threads a criar
        for(int i = 0; i < Integer.parseInt("10"); i++) {
           new  AnalisadorJSOUP("Downloader" + Integer.toString(i), l,visited_urls,urlQueue);
        }
        
        // for(int i = 0; i < Integer.parseInt(args[0]); i++)
        //TODO: JOIN
    }

    @Override
    synchronized public void putUrlInQueue(String url) throws RemoteException{
        try {
            urlQueue.putFirst(url);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
