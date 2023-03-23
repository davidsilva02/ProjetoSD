import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingQueue;

import org.jsoup.Jsoup;

public class Downloader{
    
    RMI server;
    String name;
    BlockingQueue<JSOUPData> l;

    //urls visitados
    Set <String> visited_urls;



    public Downloader(){
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
    }
    public static void main(String[] args){
        Downloader dw = new Downloader();
        dw.start();

    }
    private void start() {
        new MulticastSender("MS", this.l);

        //TODO: Colocar por argumento o numero de threads a criar
        for(int i = 0; i < Integer.parseInt("10"); i++) {
           new  AnalisadorJSOUP("Downloader" + Integer.toString(i), l,visited_urls);
        }
        
        // for(int i = 0; i < Integer.parseInt(args[0]); i++)
        //TODO: JOIN
    }

}
