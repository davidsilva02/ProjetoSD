import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Downloader implements Runnable{

    private String url;
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    MulticastSocket socket;
    InetAddress group;

    public Downloader(String url){
        this.url=url;
        try {
            this.socket=new MulticastSocket(PORT);
            this.group=InetAddress.getByName(MULTICAST_ADDRESS);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        new Thread(this,"dw").start();
    }

    @Override
    public void run() {
        System.out.printf("O DOWNLOADER COMEÇOU A INDEXAR O URL %s \n",url);


        //utilizar jsoup quando encontra um outro url para indexar cria outro Downloader (mais ou menos isto,acho)



        //envia dados para colocar nos Barrels por multicast
        try {
            socket.joinGroup(group);
            byte buffer [] = url.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
