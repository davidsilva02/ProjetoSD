package org.ProjetoSD;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Classe que armazena a informacao do url, a string url, a string title, a citacao, e um hashset que contem
 * os urls que têm referencia para o url em causa
 */
public class infoURL implements Serializable {
    private String url;
    private String title;
    private String citation;
    private HashSet<infoURL> urls = new HashSet<>();

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

    public boolean addURL(infoURL url) {
        return urls.add(url);
    }

    public int numeroURL() {
        return this.urls.size();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HashSet<infoURL> getUrls() {
        return urls;
    }

    public void setUrls(HashSet<infoURL> urls) {
        this.urls = urls;
    }

    @Override
    public String toString() {
        return String.format("Titulo: %s\nURL: %s\nCitation: %s\n", title, url, citation);
    }
}
