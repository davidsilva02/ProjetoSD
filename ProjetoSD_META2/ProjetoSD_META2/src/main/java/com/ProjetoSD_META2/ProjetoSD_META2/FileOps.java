package com.ProjetoSD_META2.ProjetoSD_META2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ConcurrentModificationException;

/**
 * Gravacão de objetos em disco
 */
public class FileOps {

	  public static Boolean writeToDisk(File file, Object obj) {
		File copy = new File(file.getAbsolutePath() + ".copy.bin");
		try {
			Files.copy(file.toPath(), copy.toPath(),StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			FileOutputStream f = new FileOutputStream(file);
			ObjectOutputStream o = new ObjectOutputStream(f);

			// Write object to disk
			o.writeObject(obj);
			o.flush();
			o.close();
			f.close();
			Files.copy(file.toPath(),copy.toPath(),StandardCopyOption.REPLACE_EXISTING);
			//copy.delete();
			return true;
		} catch (FileNotFoundException e) {
			System.out.println("Invalid Path");
			return false;
		}catch(ConcurrentModificationException e){
			return false;
		} catch (IOException e) {
			System.out.println("Error initializing stream");
			return false;
		}
		
	}

	  public static Object readFromDisk(File file) {

		Object result = null;
	  	//System.out.println(file.getAbsolutePath());

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
			//System.out.println("Error initializing stream: " + file.getPath() + " " + e);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return result;
	}

}
