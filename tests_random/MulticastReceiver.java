import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MulticastReceiver {
    MulticastSocket socket;
    InetAddress group;
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;


    public MulticastReceiver(){
        try {
            this.socket=new MulticastSocket(PORT);
            this.group=InetAddress.getByName(MULTICAST_ADDRESS);
            this.socket.joinGroup(group);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MulticastReceiver s = new MulticastReceiver();
        s.start();
    }

    private void start() {
        while(true){

            int tamanho_a_receber=0;
            byte[] buf = new byte[50];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(recv);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            byte [] option = new byte [1];
            System.arraycopy(recv.getData(),0,option,0,1);

            if (option[0]==0) {System.out.println("READ TAMANHO");}
            if (option[0]==1) {System.out.println("READ CLASS");}

            byte [] id = new byte [recv.getLength()];
            System.arraycopy(recv.getData(), 1, id, 0,4);
            String id_s = new String(id, 0, id.length);
            System.out.println(id_s);

            byte[] data = new byte[recv.getLength()-5];
            System.arraycopy(recv.getData(),5,data,0,recv.getLength()-5);


            try{
                String size = new String(data, 0, data.length);
                tamanho_a_receber=Integer.parseInt(size);
                System.out.println(tamanho_a_receber);
            }
            catch(NumberFormatException e){
                e.printStackTrace();
            }
            
        }
    }
}
