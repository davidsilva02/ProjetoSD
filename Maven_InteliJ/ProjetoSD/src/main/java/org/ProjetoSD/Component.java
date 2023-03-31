package org.ProjetoSD;

import java.io.Serializable;

/**
 * Classe que armazena o ip e o estado de cada componente
 */
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
