import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ObjDump {

    Set <String> visited_urls;

    

    public ObjDump() {
        this.visited_urls = ConcurrentHashMap.newKeySet();
    }


    public static void main(String[] args) {
        
        ObjDump n = new ObjDump();

        n.visited_urls.add("ola1");
        n.visited_urls.add("ola2");
        n.visited_urls.add("ola3");
        
        try {
			FileOutputStream f = new FileOutputStream(new File("test.bin"));
			ObjectOutputStream o = new ObjectOutputStream(f);

			// Write objects to file
			o.writeObject((Object)n.visited_urls);

			o.close();
			f.close();

            n.visited_urls.clear();

			FileInputStream fi = new FileInputStream(new File("test.bin"));
			ObjectInputStream oi = new ObjectInputStream(fi);

			// Read objects
			n.visited_urls = (Set<String>)oi.readObject();

			for(String str: n.visited_urls)
                System.out.println(str);

			oi.close();
			fi.close();

		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error initializing stream");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
}
