import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.Buffer;

public class Server{
    MulticastSocket socket;
    InetAddress group;
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    
    public Server(){
        try {
            this.socket=new MulticastSocket();
            this.group=InetAddress.getByName(MULTICAST_ADDRESS);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        Server s = new Server();
        s.start();
    }

    private void start() {
        while(true){
            int num = new Random().nextInt(9999-1000)+1000;
            byte [] id = Integer.toString(num).getBytes();
            System.out.println(num);

            byte buf[]=Integer.toString(12).getBytes();
            byte opt[]={0};

            byte [] out = new byte[buf.length + opt.length + id.length];
            System.arraycopy(opt,0, out, 0, opt.length);
            System.arraycopy(id, 0, out, opt.length, id.length);
            System.arraycopy(buf, 0,out, opt.length + id.length, buf.length);

            System.out.println(out.length);
 
            DatagramPacket packet = new DatagramPacket(out, out.length, group, PORT);
            try {
                socket.send(packet);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            opt[0]=1;
            System.arraycopy(opt,0, out, 0, opt.length);
            System.arraycopy(id, 0, out, opt.length, id.length);
            System.arraycopy(buf, 0,out, opt.length + id.length, buf.length);

            packet = new DatagramPacket(out, out.length, group, PORT);
            try {
                socket.send(packet);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }



            
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            
        }
    }


}