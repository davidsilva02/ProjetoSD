import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * Classe que envia todos os dados para os barrels
 */
public class JSOUPData implements Serializable{

    private String title;
    private String url;
    private String citation; //TODO trocar por 1 objeto infoURL?
    private List<String> termos;
    private List <String> hip;

    
    public JSOUPData(String title, String url, String citation) {
        this.title = title;
        this.url = url;
        this.citation = citation;
        this.termos=new ArrayList<>();
        this.hip=new ArrayList<>();
    }


    public void addTermo(String termo){
        this.termos.add(termo);
    }

    public void addHip(String hip){
        this.hip.add(hip);
    }


    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getCitation() {
        return citation;
    }
    public void setCitation(String citation) {
        this.citation = citation;
    }
    public List<String> getTermos() {
        return termos;
    }
    public void setTermos(List<String> termos) {
        this.termos = termos;
    }
    public List<String> getHip() {
        return hip;
    }
    public void setHip(List<String> hip) {
        this.hip = hip;
    }

    

        
}