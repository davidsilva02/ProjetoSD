package com.ProjetoSD_META2.ProjetoSD_META2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
