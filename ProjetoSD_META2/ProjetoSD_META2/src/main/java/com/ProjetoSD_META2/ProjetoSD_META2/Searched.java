package com.ProjetoSD_META2.ProjetoSD_META2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

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

    @Override
    public boolean equals(Object obj) {
        return this.getTerm().equalsIgnoreCase(((Searched) obj).getTerm());
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
