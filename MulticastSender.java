import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jsoup.Jsoup;

public class MulticastSender implements Runnable {
    RMI searchModule;
    MulticastSocket socket;
    InetAddress group;
    Thread t;
    BlockingQueue<JSOUPData> l;
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;

    public MulticastSender(String name, BlockingQueue<JSOUPData> l){
        
        super();

        this.l=l;

        // Get the reference to the server to future RMI calls
        try {
            this.searchModule= (RMI) Naming.lookup("rmi://localhost:1099/server");
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(0);
        }

        try {
            this.socket=new MulticastSocket();
            this.group=InetAddress.getByName(MULTICAST_ADDRESS);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.t=new Thread(this,name);
        t.start();
    }

    @Override
    public void run() {
        while(true){
            try {
                JSOUPData j = l.take();
                l.remove(j);

                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutputStream out;
                try {
                    out = new ObjectOutputStream(byteOut);
                    out.writeObject(j);
        
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                byte class_send [] = byteOut.toByteArray();
                int tamanho_envio=class_send.length;
    
                //enviar tamanho da classe que vamos mandar
                //se 0 vamos enviar tamanho
                byte buf[]=Integer.toString(tamanho_envio).getBytes();
                byte opt[]={0};
                byte [] send = new byte[buf.length + 1];

                //copia para o primeiro byte a opcao,de seguida copia o buf para os restantes bytes
                System.arraycopy(opt,0, send, 0, 1);
                System.arraycopy(buf, 0,send,1, buf.length);

                DatagramPacket packet = new DatagramPacket(send, send.length, group, PORT);
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                //TODO: abordagem ainda nao esta completa, temos de saber quantos barrels temos, e a busca por resposta tem de ter limites (??)
                //esperamos que todos recebam o tamanho
                HashSet<Integer> hashs = new HashSet<>();
                int number_of_barrels=1;
                while(hashs.size()!=number_of_barrels){
                    byte buffe[]=new byte[20];
                    DatagramPacket rec = new DatagramPacket(buffe, buffe.length);
                    
                    // try {
                    //     socket.setSoTimeout(10000);
                    // } catch (SocketException e) {
                    //     // TODO Auto-generated catch block
                    //     // e.printStackTrace();
                    // }
        
                    try {
                        socket.receive(rec);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                   String num = new String(rec.getData(), 0, rec.getLength());
                   hashs.add(Integer.parseInt(num));
                }
                

                System.out.printf("TODOS RECEBERAM O TAMANHO %d \n", tamanho_envio);
    
                //enviar JSOUPData com opcao 1
                opt[0]=1;
                send = new byte[class_send.length + 1];
                //copia para o primeiro byte a opcao,de seguida copia o buf para os restantes bytes
                System.arraycopy(opt,0, send, 0, 1);
                System.arraycopy(class_send, 0,send,1, class_send.length);
                packet = new DatagramPacket(send, send.length, group, PORT);
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                hashs = new HashSet<>();
                number_of_barrels=1;
                while(hashs.size()!=number_of_barrels){
                    byte buffe[]=new byte[20];
                    DatagramPacket rec = new DatagramPacket(buffe, buffe.length);
                    
                    // try {
                    //     socket.setSoTimeout(10000);
                    // } catch (SocketException e) {
                    //     // TODO Auto-generated catch block
                    //     // e.printStackTrace();
                    // }
        
                    try {
                        socket.receive(rec);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                   String num = new String(rec.getData(), 0, rec.getLength());
                   hashs.add(Integer.parseInt(num));
                }

                System.out.println("TODOS OS BARRELS RECEBERAM A CLASSE");
                
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    
}
