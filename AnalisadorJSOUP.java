import java.io.File;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class AnalisadorJSOUP implements Runnable {

    // MulticastSocket socket;
    // InetAddress group;
    RMI server;
    // String url;
    Thread t;
    BlockingQueue<JSOUPData> l;
    Set <String> visited_urls;
    String threadName;
    BlockingDeque<String> urlQueue;
    ReentrantLock lockFile1;
    Integer countIterations = 0;


    public AnalisadorJSOUP(String threadName,BlockingQueue<JSOUPData> l, Set<String> visited_urls,BlockingDeque<String> filaURL,ReentrantLock lockFile1){
        super();

        this.lockFile1=lockFile1;

        // try {
        //     this.socket=new MulticastSocket();
        //     this.group=InetAddress.getByName(MULTICAST_ADDRESS);
        //     //socket.joinGroup(group); 
        // } catch (IOException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }

        // Get the reference to the server to future RMI calls
        try {
            this.server= (RMI) Naming.lookup("rmi://localhost:3366/server");
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(0);
        }

        this.l=l;
        this.visited_urls=visited_urls;
        this.threadName =  threadName;
        this.urlQueue = filaURL;

        try {
            server.addDownloader(threadName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        

        this.t=new Thread(this,threadName);
        t.start();
    }
    
    @Override
    public void run() {

        String newUrl;
        while(true){
            newUrl = null;
            //DEBUG
            // if(visited_urls.size()==2) break;
            //ir buscar URL à queue
            while(newUrl == null){

                try{
                    synchronized(this){
                        newUrl = urlQueue.take();
                        //urlQueue.remove(newUrl);
                    }

                    //se nao existir, adicionamos que foi visitado
                    if(!visited_urls.contains(newUrl)) {
                        visited_urls.add(newUrl);

                        new Thread(() -> {
                            if(countIterations > 24){
                                lockFile1.lock();
                                FileOps.writeToDisk(new File("./DW/visitedURLS.bin"), (visited_urls));
                                lockFile1.unlock();
                                countIterations = 0;
                            }
                        }).start();

                    }
                    else {
                        System.out.println("JA VISITOU");
                        newUrl=null;
                    }
                    
                    new Thread(() -> {
                        if(countIterations > 24){
                            lockFile1.lock();
                            FileOps.writeToDisk(new File("./DW/l.bin"), (l));
                            FileOps.writeToDisk(new File("./DW/urlQ.bin"),(urlQueue));
                            lockFile1.unlock();
                            countIterations = 0;
                        }
                    }).start();


                }catch(InterruptedException e){
                    System.out.println("Exception taking an url from the queue: " +  e);
                }

                // try{
                //     //receive new url from the queue
                //     // newUrl = server.getUrl();
                //     //se nao existir, adicionamos que foi visitado
                //     if(!visited_urls.contains(newUrl)) visited_urls.add(newUrl);
                //     //se o url já tiver sido visitado, nao fazemos nada, logo escolhemos outro url
                //     else {
                //         System.out.println("JA VISITOU");
                //         newUrl=null;
                //     }  
                // }catch(Exception e){
                //     //System.out.println("Exception on downloader: " + e.getMessage());
                // }

            }

            try {
                server.makeDownloaderUnavailable(this.threadName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }



            
            // System.out.printf("O DOWNLOADER COMEÇOU A INDEXAR O URL %s \n",newUrl);
            JSOUPData j=null;
            
            //utilizar jsoup quando encontra um outro url para indexar cria outro Downloader (mais ou menos isto,acho)
            //meter tudo num objeto serializable
            // HashSet<String> urls = new HashSet<>(); //?!?!!??!?!?!?!?
            // HashMap<String,HashSet<String>> indx = new HashMap<>();
            try {

                Document doc = Jsoup.connect(newUrl).get();
                String title = doc.title();
                String citation =null;
                try{
                    citation=doc.text().substring(0,50);
                    citation+="...";
                }
                catch(StringIndexOutOfBoundsException e){
                    citation=doc.text();
                }
                
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
                    
                    synchronized(this){
                        try{
                            urlQueue.add(link.attr("abs:href"));
                        }catch(IllegalStateException e){
                            System.out.println("Exception puting an url in the queue: " +  e);
                        }
                    }
                    // server.putUrl(link.attr("abs:href")); //não seria melhor fzer isto só no fim? ns
                    // System.out.println(link.text() + "\n" + link.attr("abs:href") + "\n");
                    // urls.add(link.attr("abs:href"));
                }
                
                // //FAZER OQ COM OS URLS NOVOS?!?!!?
                // //METER NA FILA?
                // for(String str: urls)
                //     server.putUrl(str);
                

            }
            catch (Exception e) 
            {
                j=null;
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
        if(j!=null) l.add(j);
        // if(j!=null){
        //     ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        //     ObjectOutputStream out;
        //     try {
        //         out = new ObjectOutputStream(byteOut);
        //         out.writeObject(j);
    
        //     } catch (IOException e) {
        //         // TODO Auto-generated catch block
        //         e.printStackTrace();
        //     }
    
        //     byte buffer [] = byteOut.toByteArray();
        //     int tamanho_envio=buffer.length;

        //     //mandar tamanho
        //     byte buf[]=Integer.toString(tamanho_envio).getBytes();
        //     DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
        //     try {
        //         socket.send(packet);
        //     } catch (IOException e) {
        //         // TODO Auto-generated catch block
        //         e.printStackTrace();
        //     }
            
        //     //TODO: abordagem ainda nao esta completa, temos de saber quantos barrels temos, e a busca por resposta tem de ter limites (??)
        //     //esperamos que todos recebam o tamanho
        //     HashSet<Integer> hashs = new HashSet<>();
        //     int number_of_barrels=1;
        //     while(hashs.size()!=number_of_barrels){
        //         byte buffe[]=new byte[20];
        //         DatagramPacket rec = new DatagramPacket(buffe, buffe.length);
                
        //         // try {
        //         //     socket.setSoTimeout(10000);
        //         // } catch (SocketException e) {
        //         //     // TODO Auto-generated catch block
        //         //     // e.printStackTrace();
        //         // }
    
        //         try {
        //             socket.receive(rec);
        //         } catch (IOException e) {
        //             // TODO Auto-generated catch block
        //             e.printStackTrace();
        //         }
                
        //        String num = new String(rec.getData(), 0, rec.getLength());
        //        hashs.add(Integer.parseInt(num));
        //     }

        //     System.out.println("TODOS RECEBERAM O TAMANHO");

        //     //enviar JSOUPData
        //     packet = new DatagramPacket(buffer, buffer.length, group, PORT);
        //     try {
        //         socket.send(packet);
        //     } catch (IOException e) {
        //         // TODO Auto-generated catch block
        //         e.printStackTrace();
        //     }
            
        //     hashs = new HashSet<>();
        //     number_of_barrels=1;
        //     while(hashs.size()!=number_of_barrels){
        //         byte buffe[]=new byte[20];
        //         DatagramPacket rec = new DatagramPacket(buffe, buffe.length);
                
        //         // try {
        //         //     socket.setSoTimeout(10000);
        //         // } catch (SocketException e) {
        //         //     // TODO Auto-generated catch block
        //         //     // e.printStackTrace();
        //         // }
    
        //         try {
        //             socket.receive(rec);
        //         } catch (IOException e) {
        //             // TODO Auto-generated catch block
        //             e.printStackTrace();
        //         }
                
        //        String num = new String(rec.getData(), 0, rec.getLength());
        //     }
    

        //     System.out.println("TODOS RECEBERAM");
        //     }
        try {
            server.makeDownloaderAvailable(this.threadName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }


            countIterations++;
        }

    }
    
}
