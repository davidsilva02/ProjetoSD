import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileOps {

    synchronized public static Boolean writeToDisk(File file, Object obj){

        try {
			FileOutputStream f = new FileOutputStream(file);
			ObjectOutputStream o = new ObjectOutputStream(f);

            // Write object to disk
            o.writeObject(obj);

			o.close();
			f.close();
		} catch (FileNotFoundException e) {
			System.out.println("Invalid Path");
            return false;
		} catch (IOException e) {
			System.out.println("Error initializing stream");
            return false;
		}

        return true;
    }

    synchronized public static Object readFromDisk(File file){

		Object result = null;

        try {
			FileInputStream fi = new FileInputStream(file);
			ObjectInputStream oi = new ObjectInputStream(fi);

			// Read objects
			result = oi.readObject();

			oi.close();
			fi.close();

		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error initializing stream: " + e);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

        return result;
    }


}
