import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class IndexStorageBarrels extends Thread {
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    MulticastSocket socket;
    InetAddress group;

    public IndexStorageBarrels(){
        super();
        try {
            this.socket=new MulticastSocket(PORT);
            this.group=InetAddress.getByName(MULTICAST_ADDRESS);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        IndexStorageBarrels in = new IndexStorageBarrels();
        in.start();
    }


    public void run(){
        try {
            socket.joinGroup(group);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        while(true){
            byte[] buf = new byte[1000];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(recv);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            String url = new String(recv.getData(), 0, recv.getLength());
            System.out.printf("INFO A INSERIR %s \n",url);
        }
    }
}
