import java.io.ByteArrayInputStream;
import java.io.File;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class IndexStorageBarrels extends UnicastRemoteObject implements BarrelRMI{
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    MulticastSocket socket;
    InetAddress group;
    RMI server;
    String barrelName;
    ConcurrentHashMap<String,HashSet<infoURL>> ind; // termo: LINKS QUE CONTÉM O TERMO
    ConcurrentHashMap<String,infoURL> urls; // fatherUrl: LISTA DE URLS QUE FAZEM REFERENCIA PARA O fatherUrl
    ConcurrentHashMap <Integer,ArrayList<infoURL>> resultados_pesquisa;
    ReentrantLock lockFile;
    ReentrantLock lockFile1;


    public IndexStorageBarrels(String name) throws RemoteException{
        super();

        this.barrelName = name;
        File folder = new File("./ISB");
        folder.mkdir();
        
        File indBin = new File(String.format("./ISB/index_%s.bin",name));

        if( !indBin.exists() ){
            ind = new ConcurrentHashMap<>();

            try {
                indBin.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            ind = (ConcurrentHashMap<String,HashSet<infoURL>>)FileOps.readFromDisk(indBin);

            if(ind == null)
                ind = new ConcurrentHashMap<String,HashSet<infoURL>>();

        }

        File urlsBin = new File(String.format("./ISB/urls_%s.bin",name));

        if( !urlsBin.exists() ){
            urls = new ConcurrentHashMap<>();

            try {
                urlsBin.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            urls = (ConcurrentHashMap<String,infoURL>)FileOps.readFromDisk(urlsBin);

            if(urls == null)
                urls = new ConcurrentHashMap<>();

        }

        File searchResultsBin = new File(String.format("./ISB/searchResults_%s.bin",name));

        if( !searchResultsBin.exists() ){
            resultados_pesquisa = new ConcurrentHashMap<>();

            try {
                searchResultsBin.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            resultados_pesquisa = (ConcurrentHashMap <Integer,ArrayList<infoURL>>)FileOps.readFromDisk(searchResultsBin);

            if(resultados_pesquisa == null)
                resultados_pesquisa = new ConcurrentHashMap<Integer,ArrayList<infoURL>>();

        }



        try {
            this.socket=new MulticastSocket(PORT);
            this.group=InetAddress.getByName(MULTICAST_ADDRESS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.server= (RMI) Naming.lookup("rmi://localhost:1099/server");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        this.lockFile = new ReentrantLock();
        this.lockFile1 = new ReentrantLock();
    }

    public static void main(String[] args) throws RemoteException {

        Scanner sc = new Scanner(System.in);
        System.out.println("Introduza o nome do barrel:");
        String barrelName = sc.nextLine();
        sc.close();

        IndexStorageBarrels t = new IndexStorageBarrels(barrelName);
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
            server.addBarrel(this,this.barrelName);
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

            DatagramPacket recv = null;
            //enquanto nao receber a opcao correta
            while(opt!=1){
                //TODO: temos de mandar uma mensagem com o tamanho da classe que serializamos?
                    //para colocarmos aqui o tamanho certo
                    buf = new byte[tamanho_a_receber+1];
                    recv = new DatagramPacket(buf, buf.length);
                    try {
                        socket.receive(recv);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    //checa opcao
                    opt=buf[0];
                }
             
                byte data[] = new byte [recv.getLength()-1];
                System.arraycopy(recv.getData(), 1, data, 0, recv.getLength()-1);

                InetAddress senderAddress = recv.getAddress();
                int senderPort = recv.getPort();
                // System.out.println("Endereço IP do programa que enviou os dados multicast: " + senderAddress.getHostAddress());
                // System.out.println("Porta do programa que enviou os dados multicast: " + senderPort);
                
                // Crie um pacote de dados para enviar de volta ao remetente (ACK)
                byte sendData[] = Integer.toString(this.hashCode()).getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, senderAddress, senderPort);
                try {
                    socket.send(sendPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ObjectInputStream ois;
                try {
                    ois = new ObjectInputStream(bis);
                    obj = (JSOUPData) ois.readObject();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }


                
                if(obj!=null){
                    //indexar info recebida por multicast pelos downloaders
                    infoURL urlAtual=null;
                    String url=obj.getUrl();
                    System.out.println("A indexar o objeto com url " + url);
                    String title=obj.getTitle();
                    String citation=obj.getCitation();
                    HashSet <String> termos = obj.getTermos();
                    HashSet <String> hip = obj.getHip();

                        
                        if(!urls.containsKey(url)){
                            urlAtual=new infoURL(url,title, citation);
                            urls.put(url, urlAtual);
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
                                infoURL info = new infoURL(hipString);
                                info.addURL(urlAtual);
                                urls.put(hipString,info);
                            }
                            else {
                                infoURL existente =urls.get(hipString);
                                existente.addURL(urlAtual);
                            }
                        }
                    
                    
    
                    // //salvar para disco
                    // ConcurrentHashMap<?,?> copyInd = new ConcurrentHashMap<>(ind);
                    // ConcurrentHashMap<?,?> copyUrls = new ConcurrentHashMap<>(urls);

                    new Thread(() -> {
                        lockFile.lock();
                        FileOps.writeToDisk(new File(String.format("./ISB/index_%s.bin",barrelName)),ind);
                        FileOps.writeToDisk(new File(String.format("./ISB/urls_%s.bin",barrelName)),urls);
                        lockFile.unlock();
                    }).start();
                }

            }
    }
    /**
     * HashMap personalizado, de forma a conseguir obter de forma rápida os urls que aparecem x vezes nos termos, para aparecerem em primeiro lugar os urls que tem mais correspondência 
     */
    class searchTermosHash<infoURL,Integer> extends HashMap<infoURL,Integer>{
        HashMap<Integer,HashSet<infoURL>> reverse = new HashMap<>();
    
        @Override
        public Integer put(infoURL key, Integer value)
        {
            //verificar se ja existe tamanho inferior, se for o caso apagar
            int i = (int)value-1;
            if(reverse.get(i)!=null && reverse.get(i).contains(key)) reverse.get(i).remove(key);


            if(reverse.get(value)==null) reverse.put(value,new HashSet<infoURL>());
            reverse.get(value).add(key);
            
            return super.put(key, value);
        }
        public HashSet<infoURL> getKeys(Integer value) {
            return reverse.get(value);
        }

        public ArrayList<Integer> getAllKeys(){
            ArrayList<Integer> keys = new ArrayList<>(reverse.keySet());
            Collections.reverse(keys);
            return keys;            
        }
    }

    @Override
    synchronized public ArrayList<infoURL> resultadoPesquisa(String termo_pesquisa, Integer id_client) throws RemoteException {
        //marca barrel como ocupado
        //envia ao server que o barrel ja nao esta disponivel
        // try {
        //     server.notAvailableBarrel(this,this.barrelName);
        // } catch (RemoteException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }

        // System.out.printf("PESQUISA PELO TERMO %s\n",termo_pesquisa);
        // System.out.println(ind.get(termo_pesquisa));

        server.makeBarrelUnavailable(this.barrelName);

        // procurar pelo termo
        String[] termos = termo_pesquisa.split(" ");
        searchTermosHash <infoURL,Integer> urls = new searchTermosHash<>();

        for (String s:termos){
            if(ind.containsKey(s.toLowerCase())){
                for (infoURL i:ind.get(s.toLowerCase())){
                    
                    if(urls.containsKey(i)){
                        urls.put(i,urls.get(i)+1);
                    }
                    else{
                        urls.put(i, 1);
                    }

                }
            }
        }

            
            
        ArrayList<infoURL> sortedTermSearch = new ArrayList<>();
        
        for (Integer i : urls.getAllKeys()){
            //System.out.println(i);
            HashSet<infoURL> termsSearch = urls.getKeys(i);
            ArrayList <infoURL> sortedTermsSearch_ = new ArrayList<>(termsSearch);
            //ordenar pelo numero de referencias
            sortedTermsSearch_.sort(Comparator.comparing(infoURL::numeroURL));
            //System.out.println(sortedTermsSearch_);
            sortedTermSearch.addAll(sortedTermsSearch_);
        }
        

        
         //nao existem mais paginas
        if(sortedTermSearch.size()<=10) sortedTermSearch.add(new infoURL("fim"));
        else{
            resultados_pesquisa.put(id_client, new ArrayList<infoURL>(sortedTermSearch.subList(10, sortedTermSearch.size())));
            sortedTermSearch=new ArrayList<infoURL>(sortedTermSearch.subList(0, 10));
        }
        new Thread(() -> {
            lockFile1.lock();
            FileOps.writeToDisk(new File(String.format("./ISB/searchResults_%s.bin",barrelName)), (resultados_pesquisa));
            lockFile1.unlock();
        }).start();
        // if(termo_pesquisa.equals("ABC")) result="COM RESULTADOS";
        // else result="SEM RESULTADOS";

        //marca barrel como disponivel
        // try {
        //     server.AvailableBarrel(this);
        // } catch (RemoteException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }
        server.makeBarrelAvailable(this.barrelName);
        if(sortedTermSearch.size()>0) return sortedTermSearch;
        return null;           
}

    @Override
    public ArrayList<infoURL> resultsReferencesList(String url) throws RemoteException {
        if(urls.containsKey(url)){
            return new ArrayList<>(this.urls.get(url).getUrls());
        }
        else return null;
    }

    @Override
    synchronized public ArrayList<infoURL> continueSearch(Integer id_client) throws RemoteException{
        if(resultados_pesquisa.containsKey(id_client)){
            ArrayList<infoURL> l =resultados_pesquisa.get(id_client);
            ArrayList<infoURL> res=new ArrayList<infoURL>( l.subList(0, Math.min(10,l.size())));
            if(l.size()>10) resultados_pesquisa.put(id_client, new ArrayList<infoURL> (l.subList(Math.min(10,l.size()),l.size())));
            else {
                res.add(new infoURL("fim"));
                //remover id_client da pesquisa
                resultados_pesquisa.remove(id_client);
            }

            return res;
        }

        else return null;
    }
}
