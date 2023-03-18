import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Downloader{
    
    RMI server;
    String name;

    public Downloader(){
        // Get the reference to the server to future RMI calls
        try {
            this.server= (RMI) Naming.lookup("rmi://localhost:1099/server");
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(0);
        }
    }
    public static void main(String[] args){
        // Downloader dw = new Downloader();

        //TODO: Colocar por argumento o numero de threads a criar
        for(int i = 0; i < Integer.parseInt("5"); i++) {
           new  AnalisadorJSOUP("Downloader" + Integer.toString(i));
        }
        
        // for(int i = 0; i < Integer.parseInt(args[0]); i++)
        //TODO: JOIN

    }

    // private void start() {
    //     String nextUrl = null;
        
    //     while(true){
    //         try{
    //                 //receive new url from the queue on the search module
    //                 nextUrl = server.getUrl();
                    
    //         }catch(Exception e){
    //             System.out.println("Exception on downloader: " + e.getMessage());
    //         }
        
    // }
}
