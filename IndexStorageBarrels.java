import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class IndexStorageBarrels extends UnicastRemoteObject implements BarrelRMI{
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    MulticastSocket socket;
    InetAddress group;
    RMI server;
    ConcurrentHashMap<String,HashSet<infoURL>> ind; // termo: LINKS QUE CONTÉM O TERMO
    ConcurrentHashMap<String,infoURL> urls; // fatherUrl: LISTA DE URLS QUE FAZEM REFERENCIA PARA O fatherUrl


    public IndexStorageBarrels() throws RemoteException{
        super();

        this.ind= new ConcurrentHashMap<>();
        this.urls= new ConcurrentHashMap<>();

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
            int tamanho_a_receber=0;
            byte[] buf = new byte[20];
            JSOUPData obj=null;

            int opt=-1;

            //enquanto nao receber a opcao correta le sempre 
            while(opt!=0){
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(recv);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                //checa opcao
                opt=buf[0];
                
                if(opt==0){
                    byte data[] = new byte [recv.getLength()-1];
                    System.arraycopy(recv.getData(), 1, data, 0, recv.getLength()-1);

                    try{
                        String size = new String(data, 0, data.length);
                        tamanho_a_receber=Integer.parseInt(size);
                    }
                    catch(NumberFormatException e){
                        e.printStackTrace();
                    }

                    //enviamos mensagem a dizer que recebemos
                    InetAddress senderAddress = recv.getAddress();
                    int senderPort = recv.getPort();        
                    byte[] sendData = Integer.toString(this.hashCode()).getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, senderAddress, senderPort);
                    try {
                        socket.send(sendPacket);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            System.out.printf("RECEBI O TAMANHO %d", tamanho_a_receber);

            //enquanto nao receber a opcao correta
            while(opt!=1){
                //TODO: temos de mandar uma mensagem com o tamanho da classe que serializamos?
                    //para colocarmos aqui o tamanho certo
                    buf = new byte[tamanho_a_receber+1];
                    DatagramPacket recv = new DatagramPacket(buf, buf.length);
                    try {
                        socket.receive(recv);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    //checa opcao
                    opt=buf[0];

                    if(opt==1){
                        byte data[] = new byte [recv.getLength()-1];
                        System.arraycopy(recv.getData(), 1, data, 0, recv.getLength()-1);

                        InetAddress senderAddress = recv.getAddress();
                        int senderPort = recv.getPort();
                        // System.out.println("Endereço IP do programa que enviou os dados multicast: " + senderAddress.getHostAddress());
                        // System.out.println("Porta do programa que enviou os dados multicast: " + senderPort);
                        
                        // Crie um pacote de dados para enviar de volta ao remetente
                        byte sendData[] = Integer.toString(this.hashCode()).getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, senderAddress, senderPort);
                        try {
                            socket.send(sendPacket);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        
                        ByteArrayInputStream bis = new ByteArrayInputStream(data);
                        ObjectInputStream ois;
                        try {
                            ois = new ObjectInputStream(bis);
                            obj = (JSOUPData) ois.readObject();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }

                
                if(obj!=null){
                    //indexar info recebida por multicast pelos downloaders
                    String url=obj.getUrl();
                    System.out.println("A indexar o objeto com url " + url);
                    String title=obj.getTitle();
                    String citation=obj.getCitation();
                    List <String> termos = obj.getTermos();
                    List <String> hip = obj.getHip();
        
                    infoURL urlAtual=null;
                    if(!urls.containsKey(url)){
                        urlAtual=new infoURL(url,title, citation);
                    }
                    else {
                        urlAtual=urls.get(url);
                        urlAtual.setTitle(title);
                        urlAtual.setCitation(citation);
                    }
                    
                    // adicionar cada termo encontrado no index se não existir, se existir apenas adicionar o url como value
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

                // System.out.println(urls);

            }
    }

    @Override
    public List<infoURL> resultadoPesquisa(String termo_pesquisa) throws RemoteException {
        String result;

        //marca barrel como ocupado
        //envia ao server que o barrel ja nao esta disponivel
        // try {
        //     server.notAvailableBarrel(this);
        // } catch (RemoteException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }

        // procurar pelo termo
        List<infoURL> sortedTermSearch = null;
        HashSet<infoURL> termSearch = ind.get(termo_pesquisa);
        
        // se encontrarmos o termo
        if( termSearch != null ) {
            List<Integer> numReferences = new ArrayList<Integer>();

            // iterar os urls relativos ao termo encontrado
            for(infoURL newUrl: termSearch){
    
                // ir buscar o numero de urls que fazem referencia para cada URL do termo (posterior ordenacao)
                numReferences.add( urls.get(newUrl.getUrl()).getUrls().size() );
            }


            //ordenar os links da pesquisa pelo nr de referencias
            sortedTermSearch = new ArrayList<>(termSearch);
            sortedTermSearch.sort(Comparator.comparing(infoURL::numeroURL));   
        }
        // if(termo_pesquisa.equals("ABC")) result="COM RESULTADOS";
        // else result="SEM RESULTADOS";

        //marca barrel como disponivel
        // try {
        //     server.AvailableBarrel(this);
        // } catch (RemoteException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }
        
        return sortedTermSearch;
    }

    @Override
    public List<infoURL> resultsReferencesList(String url) throws RemoteException {
        return new ArrayList<>(this.urls.get(url).getUrls());
    }
}
