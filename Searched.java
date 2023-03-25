import java.io.Serializable;
import java.util.Comparator;

// class SearchedComparator implements Comparator<Searched>, Serializable{

//     @Override
//     public int compare(Searched s1, Searched s2) {
//         if (s1.getNumSearches() < s2.getNumSearches())
//             return 1;
//         else if (s1.getNumSearches() > s2.getNumSearches())
//             return -1;
//         return 0;

//     }

// }

public class Searched implements Serializable {

    private Integer numSearches;
    private String term;

    public Searched(Integer numSearches, String term) {
        this.numSearches = numSearches;
        this.term = term;
    }
    public Integer getNumSearches() {
        return numSearches;
    }
    public void setNumSearches(Integer numSearches) {
        this.numSearches = numSearches;
    }
    public String getTerm() {
        return term;
    }
    public void setTerm(String term) {
        this.term = term;
    }

    // @Override
    // public int compare(Searched s1, Searched s2) {
    //     if (s1.getNumSearches() < s2.getNumSearches())
    //         return 1;
    //     else if (s1.getNumSearches() > s2.getNumSearches())
    //         return -1;
    //     return 0;

    // }

    @Override
    public boolean equals(Object obj) {
        return this.getTerm().equalsIgnoreCase( ((Searched)obj).getTerm() );
    }

    // @Override
    // public boolean equals(Object s1, Object s2) {
    //     return getTerm().equals( ((Searched)searched).getTerm());
    // }
    
    
}
