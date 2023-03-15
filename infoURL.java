import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class infoURL {
    private String url;
    private String title;
    private String citation;
    private HashSet <infoURL> urls= new HashSet<>();
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getCitation() {
        return citation;
    }
    public void setCitation(String citation) {
        this.citation = citation;
    }
    public infoURL(String url, String title, String citation) {
        this.url = url;
        this.title = title;
        this.citation = citation;
    }

    public infoURL(String url) {
        this.url = url;
    }
    public boolean addURL(infoURL url){
        return urls.add(url);
    }

    public int numeroURL(infoURL url){
        return urls.size();
    }
}
