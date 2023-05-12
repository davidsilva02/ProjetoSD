package com.ProjetoSD_META2.ProjetoSD_META2;

import java.rmi.*;
import java.util.ArrayList;

public interface BarrelRMI extends Remote {
	public ArrayList<infoURL> resultsReferencesList(String url) throws java.rmi.RemoteException;
	public ArrayList<infoURL> resultadoPesquisa(String termo_pesquisa,String id_client) throws java.rmi.RemoteException;
	public ArrayList<infoURL> continueSearch(String id_client) throws java.rmi.RemoteException;

	public String verify() throws RemoteException;
}