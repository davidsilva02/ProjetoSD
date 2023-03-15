import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class IndexStorageBarrels extends UnicastRemoteObject implements BarrelRMI{
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    MulticastSocket socket;
    InetAddress group;
    RMI server;
    HashMap<String,HashSet<infoURL>> ind;
    HashMap<String,infoURL> urls;


    public IndexStorageBarrels() throws RemoteException{
        super();

        this.ind= new HashMap<>();
        this.urls= new HashMap<>();

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
        IndexStorageBarrels t = new IndexStorageBarrels();
        System.out.printf("BARREL WITH HASH %d\n", t.hashCode());        
        t.start();
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
            server.AvailableBarrel(this);
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
            
            String pack = new String(recv.getData(), 0, recv.getLength());
            System.out.printf("INFO A INSERIR %s \n",pack);

            
            //indexar info recebida por multicast pelos downloaders
            String url="";
            String title="";
            String citation="";
            List <String> termos = new ArrayList<>();
            List <String> hip = new ArrayList<>();

            infoURL urlAtual=null;
            if(!urls.containsKey(url)){
                urlAtual=new infoURL(url,title, citation);
            }
            else {
                urlAtual=urls.get(url);
                urlAtual.setTitle(title);
                urlAtual.setCitation(citation);
            }

            for (String termoString: termos){
                if(!ind.containsKey(termoString)){
                    //temos de criar entrada                
                    HashSet<infoURL> urls = new HashSet<>();
                    urls.add(urlAtual);
                    ind.put(termoString,urls);
                }
                else {
                    ind.get(termoString).add(urlAtual);
                }
            }
    
            for (String hipString : hip){
                if(!urls.containsKey(hipString)){
                    infoURL info = new infoURL(url);
                    info.addURL(urlAtual);
                    urls.put(hipString,info);
                }
                else {
                    infoURL existente =urls.get(hipString);
                    existente.addURL(urlAtual);
                }
            }
        }
    }

    @Override
    public String resultadoPesquisa(String termo_pesquisa) throws RemoteException {
        String result;

        //marca barrel como ocupado
         //envia ao server que o barrel ja nao esta disponivel
         try {
            server.notAvailableBarrel(this);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        if(termo_pesquisa.equals("ABC")) result="COM RESULTADOS";
        else result="SEM RESULTADOS";


        //marca barrel como disponivel
        try {
            server.AvailableBarrel(this);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return result;
    }
}
