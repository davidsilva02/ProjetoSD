package org.ProjetoSD_MAVEN;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Classe que envia todos os dados para os barrels
 */
public class JSOUPData implements Serializable {

    private String title;
    private String url;
    private String citation;
    private HashSet<String> termos;
    private HashSet<String> hip;

    public JSOUPData(String title, String url, String citation) {
        this.title = title;
        this.url = url;
        this.citation = citation;
        this.termos = new HashSet<>();
        this.hip = new HashSet<>();
    }

    public void addTermo(String termo) {
        this.termos.add(termo);
    }

    public void addHip(String hip) {
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

    public HashSet<String> getTermos() {
        return termos;
    }

    public void setTermos(HashSet<String> termos) {
        this.termos = termos;
    }

    public HashSet<String> getHip() {
        return hip;
    }

    public void setHip(HashSet<String> hip) {
        this.hip = hip;
    }

}