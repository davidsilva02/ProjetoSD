import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

public class AnalisadorJSOUP implements Runnable {

    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    MulticastSocket socket;
    InetAddress group;
    RMI server;
    String url;


    public AnalisadorJSOUP(String threadName){
        super();

        try {
            this.socket=new MulticastSocket();
            this.group=InetAddress.getByName(MULTICAST_ADDRESS);
            //socket.joinGroup(group); 
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

        new Thread(this,threadName).start();
    }
    
    @Override
    public void run() {

        String newUrl;
        int numTries = 5;
        int timeout = 700;
        Boolean ack;

        while(true){
            newUrl = null;
            ack = false;
            
            //ir buscar URL à queue
            while(newUrl == null){
                try{
                    //receive new url from the queue on the search module
                    newUrl = server.getUrl();
                    
                }catch(Exception e){
                    System.out.println("Exception on downloader: " + e.getMessage());
                }
            }
            
            System.out.printf("O DOWNLOADER COMEÇOU A INDEXAR O URL %s \n",newUrl);
            JSOUPData j=null;
            
            //utilizar jsoup quando encontra um outro url para indexar cria outro Downloader (mais ou menos isto,acho)
            //meter tudo num objeto serializable
            // HashSet<String> urls = new HashSet<>(); //?!?!!??!?!?!?!?
            // HashMap<String,HashSet<String>> indx = new HashMap<>();
            try {

                Document doc = Jsoup.connect(newUrl).get();
                String title = doc.title();
                //so para testar citacao
                String citation=doc.text().substring(0,5);

                StringTokenizer tokens = new StringTokenizer(doc.text());
                j=new JSOUPData(title, newUrl, citation);
                while (tokens.hasMoreElements()){
                    // System.out.println(tokens.nextToken().toLowerCase());
                    j.addTermo(tokens.nextToken().toLowerCase());
                    // //adicionar ao index
                    // if( !indx.containsKey(tokens.nextToken())){ //não está no index
                    //     HashSet<String> tempUrls = new HashSet<String>();
                    //     tempUrls.add(newUrl);
                    //     indx.put(tokens.nextToken(),tempUrls);
                    // }
                    // else{ //está no index
                    //     HashSet<String> tempUrls = indx.get(tokens.nextToken());
                    //     tempUrls.add(newUrl);
                    //     indx.put(tokens.nextToken(),tempUrls);
                    // }
                } 

                //get urls
                Elements links = doc.select("a[href]");
                for (Element link : links){    
                    j.addHip(link.attr("abs:href"));
                    server.putUrl(link.attr("abs:href"));
                    // System.out.println(link.text() + "\n" + link.attr("abs:href") + "\n");
                    // urls.add(link.attr("abs:href"));
                }
                
                // //FAZER OQ COM OS URLS NOVOS?!?!!?
                // //METER NA FILA?
                // for(String str: urls)
                //     server.putUrl(str);
                

            }
            catch (IOException e) 
            {
                e.printStackTrace();
            }

            
        //     //envia dados para colocar nos Barrels por multicast
        //     for(int i = 0; i < numTries || ack; i++){
        //         try {
        //             //Convert HashMap to ByteArray
        //             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        //             ObjectOutputStream out = new ObjectOutputStream(byteOut);
        //             out.writeObject(indx);
        //             byte buffer [] = byteOut.toByteArray();
                    
        //             //send the hashmap (index) to the ISB
        //             DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
        //             socket.send(packet);

        //             //timeout e get ack
        //             socket.setSoTimeout(timeout);

        //             DatagramPacket ackDgram = new DatagramPacket(buffer, buffer.length);	
		// 			socket.receive(ackDgram);

        //             ack = true;

        //         } catch (Exception e) {
        //             e.printStackTrace();
        //         }
        //     }
        // }

        //TODO: Algumas excecoes ocorrem em cima e manda o objeto a null
        if(j!=null){
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out;
            try {
                out = new ObjectOutputStream(byteOut);
                out.writeObject(j);
    
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    
            byte buffer [] = byteOut.toByteArray();
            System.out.println(buffer.length);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            try {
                socket.send(packet);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    
            //TODO: abordagem ainda nao esta completa, temos de saber quantos barrels temos, e a busca por resposta tem de ter limites (??)
            
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
    
    
            
            System.out.println("TODOS RECEBERAM");
            }

        }

    }
    
}
