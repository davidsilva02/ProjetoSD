package com.ProjetoSD_META2.ProjetoSD_META2;

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
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread que retira da fila downloader e faz crawler do url e adiciona a info na fila 
 * de mensagens Multicast
 */
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

                        if(countIterations == 25){
                        new Thread(() -> {
                                lockFile1.lock();
                                FileOps.writeToDisk(new File("./DW/visitedURLS.bin"), (visited_urls));
                                lockFile1.unlock();
                            }).start();
                            countIterations = 0;
                        }

                    }
                    else {
                        System.out.println("JA VISITOU");
                        newUrl=null;
                    }
                    
                    if(countIterations == 25){
                    new Thread(() -> {
                            lockFile1.lock();
                            FileOps.writeToDisk(new File("./DW/l.bin"), (l));
                            FileOps.writeToDisk(new File("./DW/urlQ.bin"),(urlQueue));
                            lockFile1.unlock();
                        }).start();
                        countIterations = 0;
                    }


                }catch(InterruptedException e){
                    System.out.println("Exception taking an url from the queue: " +  e);
                }
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
                   
                }
                
            }
            catch (Exception e) 
            {
                j=null;
                e.printStackTrace();
            }

        //TODO: Algumas excecoes ocorrem em cima e manda o objeto a null
        if(j!=null) l.add(j);

        try {
            server.makeDownloaderAvailable(this.threadName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
            countIterations++;
        }

    }
    
}
