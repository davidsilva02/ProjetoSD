import java.io.File;
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
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.jsoup.Jsoup;
import org.w3c.dom.views.DocumentView;

public class Downloader extends UnicastRemoteObject implements DownloaderRMI{
    
    RMI server;
    String name;
    BlockingQueue<JSOUPData> l;
    private BlockingDeque<String> urlQueue;
    
    //urls visitados
    Set <String> visited_urls;

    
    AtomicInteger number_barrels;
    Object lock_changes;

    ReentrantLock lockFileJSOUP;



    public Downloader() throws RemoteException{

        // Get the reference to the server to future RMI calls
        try {
            this.server= (RMI) Naming.lookup("rmi://localhost:3366/server");
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            e.printStackTrace();
            System.exit(0);
        }

        //não existe um concurrent HashMap, entao temos de transformar isto num set
        File folder = new File("./DW");
        folder.mkdir();
        
        File visitedBin = new File("./DW/visitedUrls.bin");
        File copy = new File(visitedBin.getAbsolutePath() + ".copy.bin");

        

        if(!copy.exists() && visitedBin.exists()){
            this.visited_urls = (KeySetView<String,Boolean>)FileOps.readFromDisk(visitedBin);
            
            
            if(this.visited_urls == null)
                this.visited_urls = ConcurrentHashMap.newKeySet();
        }
        else if (copy.exists() && visitedBin.exists()){
            this.visited_urls = (KeySetView<String,Boolean>)FileOps.readFromDisk(copy);
            if(this.visited_urls == null){
                if(visitedBin.exists())
                    this.visited_urls = (KeySetView<String,Boolean>)FileOps.readFromDisk(visitedBin);
                else
                    this.visited_urls = ConcurrentHashMap.newKeySet();
            }
        }
        //le do disco
        else{
            this.visited_urls = ConcurrentHashMap.newKeySet();
            try {
                visitedBin.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        File urlBin = new File("./DW/urlQ.bin");
        copy = new File(urlBin.getAbsolutePath() + ".copy.bin");

        if(!copy.exists() && urlBin.exists()){
            this.urlQueue = (LinkedBlockingDeque<String>)FileOps.readFromDisk(urlBin);
            
            
            if(this.urlQueue == null)
                this.urlQueue = new LinkedBlockingDeque<>();
        }
        else if (copy.exists() && urlBin.exists()){
            this.urlQueue = (LinkedBlockingDeque<String>)FileOps.readFromDisk(copy);
            if(this.urlQueue == null){
                if(urlBin.exists())
                    this.urlQueue = (LinkedBlockingDeque<String>)FileOps.readFromDisk(urlBin);
                else
                    this.urlQueue = new LinkedBlockingDeque<String>();
            }
        }
        //le do disco
        else{
            this.urlQueue = new LinkedBlockingDeque<>();
            try {
                urlBin.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File lBin = new File("./DW/l.bin");
        copy = new File (lBin.getAbsolutePath() + ".copy.bin");

        
        if(!copy.exists() && lBin.exists()){
            this.l = new LinkedBlockingQueue<>();

            if(this.l == null)
                this.l = new LinkedBlockingQueue<>();
        }
        else if (copy.exists() && lBin.exists()){
            this.l = (LinkedBlockingQueue<JSOUPData>)FileOps.readFromDisk(copy);
            if(this.l == null){
                if(lBin.exists())
                    this.l = (LinkedBlockingQueue<JSOUPData>)FileOps.readFromDisk(lBin);
                else
                    this.l = new LinkedBlockingQueue<>();
            }
        }
        else{
            this.l = new LinkedBlockingQueue<>();
            try {
                lBin.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.lock_changes=new Object();
        this.number_barrels=new AtomicInteger();
        lockFileJSOUP=new ReentrantLock();
    }
    public static void main(String[] args){

        // Create object
        Downloader dw = null;
        try {
            dw = new Downloader();
        } catch (RemoteException e) {
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

        new MulticastSender("MS", this.l,number_barrels,lock_changes);


        //TODO: Colocar por argumento o numero de threads a criar
        for(int i = 0; i < Integer.parseInt("20"); i++) {
           new  AnalisadorJSOUP("Downloader" + Integer.toString(i), l,visited_urls,urlQueue,lockFileJSOUP);
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
    @Override
    public void updateNumberBarrels(Integer n) throws RemoteException {
        //provavelmente antes era 0, fazer notify
        if(n==1){
            synchronized(lock_changes){
                lock_changes.notify();
            }
        }

        number_barrels.set(n);
    }

}
