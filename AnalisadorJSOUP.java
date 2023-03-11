import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class AnalisadorJSOUP implements Runnable {

    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    MulticastSocket socket;
    InetAddress group;
    RMI server;

    String url;


    public AnalisadorJSOUP(String url){
        super();

        try {
            this.socket=new MulticastSocket(PORT);
            this.group=InetAddress.getByName(MULTICAST_ADDRESS);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Get the reference to the server to future RMI calls
        try {
            this.server= (RMI) Naming.lookup("rmi://localhost:1099/server");
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.url=url;

        new Thread(this,"dw").start();
    }
    
    @Override
    public void run() {
        System.out.printf("O DOWNLOADER COMEÇOU A INDEXAR O URL %s \n",url);


        //utilizar jsoup quando encontra um outro url para indexar cria outro Downloader (mais ou menos isto,acho)


        
        //envia dados para colocar nos Barrels por multicast
        try {
            socket.joinGroup(group);
            byte buffer [] = this.url.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
