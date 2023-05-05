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

public class AnalisadorJSOUP implements Runnable {

    // MulticastSocket socket;
    // InetAddress group;
    RMI server;
    // String url;
    Thread t;
    BlockingQueue<JSOUPData> l;
    Set<String> visited_urls;
    String threadName;
    BlockingDeque<String> urlQueue;
    ReentrantLock lockFile1;
    Integer countIterations = 0;

    public AnalisadorJSOUP(String threadName, BlockingQueue<JSOUPData> l, Set<String> visited_urls,
            BlockingDeque<String> filaURL, ReentrantLock lockFile1) {
        super();

        this.lockFile1 = lockFile1;

        // Get the reference to the server to future RMI calls
        try {
            this.server = (RMI) Naming.lookup("rmi://localhost:3366/server");
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            e.printStackTrace();
            System.exit(0);
        }

        this.l = l;
        this.visited_urls = visited_urls;
        this.threadName = threadName;
        this.urlQueue = filaURL;

        try {
            server.addDownloader(threadName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        this.t = new Thread(this, threadName);
        t.start();
    }

    @Override
    public void run() {

        String newUrl;
        while (true) {
            newUrl = null;

            while (newUrl == null) {

                try {
                    synchronized (this) {
                        newUrl = urlQueue.take();
                    }

                    // se nao existir, adicionamos que foi visitado
                    if (!visited_urls.contains(newUrl)) {
                        visited_urls.add(newUrl);

                        new Thread(() -> {
                            if (countIterations > 24) {
                                lockFile1.lock();
                                FileOps.writeToDisk(new File("./DW/visitedURLS.bin"), (visited_urls));
                                lockFile1.unlock();
                                countIterations = 0;
                            }
                        }).start();

                    } else {
                        System.out.println("JA VISITOU");
                        newUrl = null;
                    }

                    new Thread(() -> {
                        if (countIterations > 24) {
                            lockFile1.lock();
                            FileOps.writeToDisk(new File("./DW/l.bin"), (l));
                            FileOps.writeToDisk(new File("./DW/urlQ.bin"), (urlQueue));
                            lockFile1.unlock();
                            countIterations = 0;
                        }
                    }).start();

                } catch (InterruptedException e) {
                    System.out.println("Exception taking an url from the queue: " + e);
                }

            }

            try {
                server.makeDownloaderUnavailable(this.threadName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            JSOUPData j = null;

            try {

                Document doc = Jsoup.connect(newUrl).get();
                String title = doc.title();
                String citation = null;
                try {
                    citation = doc.text().substring(0, 50);
                    citation += "...";
                } catch (StringIndexOutOfBoundsException e) {
                    citation = doc.text();
                }

                StringTokenizer tokens = new StringTokenizer(doc.text());
                j = new JSOUPData(title, newUrl, citation);
                while (tokens.hasMoreElements()) {
                    // System.out.println(tokens.nextToken().toLowerCase());
                    j.addTermo(tokens.nextToken().toLowerCase());
                }

                // get urls
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    j.addHip(link.attr("abs:href"));

                    synchronized (this) {
                        try {
                            urlQueue.add(link.attr("abs:href"));
                        } catch (IllegalStateException e) {
                            System.out.println("Exception puting an url in the queue: " + e);
                        }
                    }

                }

            } catch (Exception e) {
                j = null;
                e.printStackTrace();
            }

            if (j != null)
                l.add(j);

            try {
                server.makeDownloaderAvailable(this.threadName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            countIterations++;
        }

    }

}
