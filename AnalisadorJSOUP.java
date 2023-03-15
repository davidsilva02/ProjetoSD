import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.StringTokenizer;

public class AnalisadorJSOUP implements Runnable {

    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    MulticastSocket socket;
    InetAddress group;
    RMI server;

    String url;


    public AnalisadorJSOUP(String url){
        super();

        try {
            this.socket=new MulticastSocket(PORT);
            this.group=InetAddress.getByName(MULTICAST_ADDRESS);
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

        this.url=url;

        new Thread(this,"dw").start();
    }
    
    @Override
    public void run() {
        System.out.printf("O DOWNLOADER COMEÇOU A INDEXAR O URL %s \n",url);


        //utilizar jsoup quando encontra um outro url para indexar cria outro Downloader (mais ou menos isto,acho)
        try {
            Document doc = Jsoup.connect(url).get();
            StringTokenizer tokens = new StringTokenizer(doc.text());
            int countTokens = 0;
            while (tokens.hasMoreElements() && countTokens++ < 100) System.out.println(tokens.nextToken().toLowerCase());
            //Elements links = doc.select("a[href]");
            //for (Element link : links)System.out.println(link.text() + "\n" + link.attr("abs:href") + "\n");
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }

        
        //envia dados para colocar nos Barrels por multicast
        try {
            socket.joinGroup(group);
            byte buffer [] = this.url.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
