package org.ProjetoSD;

import java.rmi.*;
import java.util.ArrayList;

public interface BarrelRMI extends Remote {
	public ArrayList<infoURL> resultsReferencesList(String url) throws java.rmi.RemoteException;
	public ArrayList<infoURL> resultadoPesquisa(String termo_pesquisa,Integer id_client) throws java.rmi.RemoteException;
	public ArrayList<infoURL> continueSearch(Integer id_client) throws java.rmi.RemoteException;
}