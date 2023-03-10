import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

public class IndexStorageBarrels extends UnicastRemoteObject implements BarrelsRMI{
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    private static int rmi_port;
    MulticastSocket socket;
    InetAddress group;
    RMI server;

    public IndexStorageBarrels() throws RemoteException{
        super();

        try {
            this.socket=new MulticastSocket(PORT);
            this.group=InetAddress.getByName(MULTICAST_ADDRESS);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            this.server= (RMI) Naming.lookup("rmi://localhost:1099/server");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void main(String[] args) throws RemoteException {
        IndexStorageBarrels h = new IndexStorageBarrels();

        rmi_port=(int) (Math.random()* (10000 - 1000 + 1) + 1000);

    
        //cria registo rmi para o server comunicar com o barrel
        LocateRegistry.createRegistry(rmi_port).rebind("barrel", h);

        System.out.printf("BARREL WITH PORT %d\n", rmi_port);        
        IndexStorageBarrels in = new IndexStorageBarrels();
        in.start();
    }
    
    public void start(){

        //adiciona socket multicast
        try {
            socket.joinGroup(group);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

         //envia ao server que existe um barrel disponivel
         try {
            server.connectBarrel(rmi_port);
        } catch (RemoteException e) {
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

    @Override
    public String resultadoPesquisa(String termo_pesquisa) throws RemoteException {
        String result;

        //marca barrel como ocupado
         //envia ao server que o barrel ja nao esta disponivel
         try {
            server.notAvailableBarrel(rmi_port);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        if(termo_pesquisa.equals("ABC")) result="COM RESULTADOS";
        else result="SEM RESULTADOS";


        //marca barrel como disponivel
        try {
            server.connectBarrel(rmi_port);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return result;
    }
}
