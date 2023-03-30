package org.ProjetoSD_MAVEN;

import java.io.Serializable;

public class Component implements Serializable{

    private String ip;
    private Boolean isAvailable;

    
    public Component(String ip, Boolean isAvailable) {
        this.ip = ip;
        this.isAvailable = isAvailable;
    }


    public String getIp() {
        return ip;
    }


    public void setIp(String ip) {
        this.ip = ip;
    }
    public Boolean getIsAvailable() {
        return isAvailable;
    }
    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    
    
}
