package tests_random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

class Teste{
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
public class EstrutasTesting {
    HashMap<String,TreeSet<Teste>> h =new HashMap<String,TreeSet<Teste>
    public static void main(String[] args) {
        EstrutasTesting e = new EstrutasTesting();
        e.start();
    }

    private void start() {
        h.add(new Teste("A"));
        h.add(new Teste("F"));
        h.add(new Teste("T"));
        h.add(new Teste("K"));

        System.out.println(h);




        // List<Teste> t = new ArrayList<>(h);
        // t.sort(Comparator.comparing(Teste::getName,
        // (s1,s2)->{return ((String) s1).compareTo((String) s2);}));

        // List<List<Teste>> agrupado=new ArrayList<>();

        // for (int i=0;i<t.size();i+=10){
        //     agrupado.add(t.subList(i,Math.min(i+10, t.size())));
        // }
        

        // System.out.println(h);
        // System.out.println(t);
        // System.out.println(agrupado);


    }

    
}
