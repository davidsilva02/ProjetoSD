
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

class Teste implements Serializable{
    private String name;
    private int references;

    public Teste(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    

    @Override
    public String toString() {
        return "Teste [name=" + name + ", references=" + references + "]";
    }

    public int getReferences() {
        return references;
    }

    public void setReferences(int references) {
        this.references = references;
    }
}

class dataClass implements Serializable{
    Set<Teste> h;
    File f;

    public dataClass(File f){
        this.f=f;
        if(!loadingSet()){
            this.h=ConcurrentHashMap.newKeySet();
        }
    }

    private boolean loadingSet(){
        Boolean flag=false;
        //se ficheiro nao existir
        if(!f.exists()) return flag;
        else {
            try{
                FileInputStream fis = new FileInputStream(f);
                ObjectInputStream ois = new ObjectInputStream(fis);
                dataClass raw = (dataClass) ois.readObject();

                this.h=raw.h;
                flag=true;
            }
            catch(IOException ioe){
                ioe.printStackTrace();
            }
            catch (ClassNotFoundException c){
                System.out.println("ClassNotFoundException c");
            }
        }

        return flag;
    }

    public void save() {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

class MyHashMap<String,Integer> extends HashMap<String,Integer>{
    HashMap<Integer,HashSet<String>> reverse = new HashMap<>();

    @Override
    public Integer put(String key, Integer value)
    {
        //verificar se ja existe tamanho inferior
        int i = (int)value-1;
        System.out.println(i);
        if(reverse.get(i)!=null && reverse.get(i).contains(key)) reverse.get(i).remove(key);
        if(reverse.get(value)==null){
                reverse.put(value,new HashSet<String>());
        }
        reverse.get(value).add(key);
        
        return super.put(key, value);
    }
    public HashSet<String> getKeys(Integer value) {
        return reverse.get(value);
    }
}
    
public class EstrutasTesting {
    MyHashMap<String,Integer> h;
    public static void main(String[] args) {
        EstrutasTesting e = new EstrutasTesting();
        e.start();
    }
    
    private void start() {
        this.h=new MyHashMap<>();
        h.put("A", 1);
        h.put("A", 2);
        h.put("B",2);
        System.out.println(h.getKeys(2));


    }
}
