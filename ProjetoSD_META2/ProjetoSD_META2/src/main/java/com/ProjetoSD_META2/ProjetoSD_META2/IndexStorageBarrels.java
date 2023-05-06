package com.ProjetoSD_META2.ProjetoSD_META2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Armazenamento da info recolhida pelos crawlers, de forma ao Client puder
 * fazer pesquisas de forma rápida e eficiente
 */
public class IndexStorageBarrels extends UnicastRemoteObject implements BarrelRMI {
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    MulticastSocket socket;
    InetAddress group;
    RMI server;
    String barrelName;
    ConcurrentHashMap<String, HashSet<infoURL>> ind; // termo: LINKS QUE CONTÉM O TERMO
    ConcurrentHashMap<String, infoURL> urls; // fatherUrl: LISTA DE URLS QUE FAZEM REFERENCIA PARA O fatherUrl
    ConcurrentHashMap<Integer, ArrayList<infoURL>> resultados_pesquisa;
    ReentrantLock lockFile;
    ReentrantLock lockFile1;
    Integer countIterations = 0;

    private String path_db;

    public IndexStorageBarrels(String name) throws RemoteException {
        super();

        this.barrelName = name;

        try {
            this.socket = new MulticastSocket(PORT);
            this.group = InetAddress.getByName(MULTICAST_ADDRESS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.server = (RMI) Naming.lookup("rmi://localhost:3366/server");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        resultados_pesquisa = new ConcurrentHashMap<>();

        this.lockFile = new ReentrantLock();
        this.lockFile1 = new ReentrantLock();

        this.resultados_pesquisa=new ConcurrentHashMap<>();
    }

    public Connection makeConnection(){
        //conectar ao sqlite
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(path_db);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }

        return conn;
    }

    public boolean makeQuery(String sql){
        boolean status=false;


        Connection c = makeConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            stmt.execute(sql);
            status=true;
        } catch (SQLException e) {
            status=false;
            e.printStackTrace();
        }

        try {
            stmt.close();
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return status;
    }

    public boolean verificateAndCreateTables(){
        boolean status=false;
        String sql = null;

        //create table of urls
        sql= """
            CREATE TABLE IF NOT EXISTS url(
                url VARCHAR(255) NOT NULL PRIMARY KEY,
                titulo VARCHAR(80),
                citacao VARCHAR(50));
             """;

        status=makeQuery(sql);

        //create table of urls, terms
        sql= """
            CREATE TABLE IF NOT EXISTS indice_invertido(
                termo VARCHAR(255) NOT NULL,
                url_termo VARCHAR(255) NOT NULL,
                PRIMARY KEY (termo,url_termo),
                FOREIGN KEY (url_termo) REFERENCES url(url));
             """;

        if(status) status=makeQuery(sql);

        //create table of url_referencia, terms
        sql= """
            CREATE TABLE IF NOT EXISTS url_referencia (
               url VARCHAR(255) NOT NULL,
               url_referencia VARCHAR(255) NOT NULL,
               PRIMARY KEY (url,url_referencia),
               FOREIGN KEY (url) REFERENCES url(url),
               FOREIGN KEY (url_referencia) REFERENCES url(url));
             """;

        if(status) status=makeQuery(sql);

        //create table of url_referencia, terms
        sql= """
            CREATE TABLE IF NOT EXISTS contagem_referencias (
               url VARCHAR(255) NOT NULL,
               count INTEGER DEFAULT 0,
               PRIMARY KEY (url),
               FOREIGN KEY (url) REFERENCES url(url));
             """;

        if(status) status=makeQuery(sql);

        return status;
    }

    private boolean inserirURL(JSOUPData u) {
        boolean status=false;
        Connection c = makeConnection();
        String sql=null;
        PreparedStatement s;

        try {
            c.setAutoCommit(false);

            //inserir url
            sql= "INSERT OR REPLACE INTO url (url,titulo,citacao) VALUES (?,?,?);";
            s = c.prepareStatement(sql);
            s.setString(1,u.getUrl());
            s.setString(2,u.getTitle());
            s.setString(3,u.getCitation());
            s.executeUpdate();

            //inserir url nos termos
            for (String termo:u.getTermos()){
                sql="INSERT OR REPLACE INTO indice_invertido(termo,url_termo) VALUES (?,?);";
                s = c.prepareStatement(sql);
                s.setString(1,termo);
                s.setString(2,u.getUrl());
                s.executeUpdate();
            }

            //inserir urls que fazem referencia
            for (String url:u.getHip()){
                if(!u.getUrl().equals(url)){
                    sql="INSERT OR IGNORE INTO url_referencia (url,url_referencia) VALUES (?,?);";
                    s = c.prepareStatement(sql);
                    s.setString(1,url);
                    s.setString(2,u.getUrl());
                    //adicionar na tabela contagem
                    if(s.executeUpdate()!=0) {
                        sql = "INSERT OR IGNORE INTO contagem_referencias (url) VALUES (?);";
                        s=c.prepareStatement(sql);
                        s.setString(1,url);
                        s.executeUpdate();
                        sql = "UPDATE contagem_referencias SET count=count+1 where url = ?;";
                        s=c.prepareStatement(sql);
                        s.setString(1,url);
                        s.executeUpdate();
                    }
                }
            }

            c.commit();
            s.close();
            c.close();
            status=true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return status;
    }
    public static void main(String[] args) throws RemoteException {

        Scanner sc = new Scanner(System.in);
        System.out.println("Introduza o nome do barrel:");
        String barrelName = sc.nextLine();
        sc.close();

        IndexStorageBarrels t = new IndexStorageBarrels(barrelName);
        System.out.printf("BARREL WITH HASH %d\n", t.hashCode());
        t.start();
    }

    public void start() {
        path_db = "jdbc:sqlite:" + new File(this.barrelName + ".db").getAbsolutePath();

        if (!verificateAndCreateTables()) System.out.println("ERRO: CRIAÇÃO DE TABELAS");


        // adiciona socket multicast
        try {
            socket.joinGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // envia ao server que existe um barrel disponivel
        try {
            server.addBarrel(this, this.barrelName);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(1);
        }


        while (true) {
            int tamanho_a_receber = 0;
            byte[] buf = new byte[20];
            JSOUPData obj = null;

            int opt = -1;

            // enquanto nao receber a opcao correta le sempre
            while (opt != 0) {
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(recv);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // checa opcao
                opt = buf[0];

                if (opt == 0) {
                    byte data[] = new byte[recv.getLength() - 1];
                    System.arraycopy(recv.getData(), 1, data, 0, recv.getLength() - 1);

                    try {
                        String size = new String(data, 0, data.length);
                        tamanho_a_receber = Integer.parseInt(size);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    // enviamos mensagem a dizer que recebemos
                    InetAddress senderAddress = recv.getAddress();
                    int senderPort = recv.getPort();
                    byte[] sendData = Integer.toString(this.hashCode()).getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, senderAddress,
                            senderPort);
                    try {
                        socket.send(sendPacket);
                    } catch (IOException e) {
                    }
                }
            }

/*
            System.out.printf("RECEBI O TAMANHO %d", tamanho_a_receber);
*/

            DatagramPacket recv = null;
            // enquanto nao receber a opcao correta
            while (opt != 1) {
                buf = new byte[tamanho_a_receber + 1];
                recv = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(recv);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // checa opcao
                opt = buf[0];
            }

            byte dataToUncompress[] = new byte[recv.getLength() - 1];
            System.arraycopy(recv.getData(), 1, dataToUncompress, 0, recv.getLength() - 1);

            // descomprime os dados
            Inflater decompressor = new Inflater();
            decompressor.setInput(dataToUncompress);
            byte data[] = new byte[tamanho_a_receber];

            try {
                decompressor.inflate(data);
            } catch (DataFormatException e) {
                e.printStackTrace();
            }

            InetAddress senderAddress = recv.getAddress();
            int senderPort = recv.getPort();
            // System.out.println("Endereço IP do programa que enviou os dados multicast: "
            // + senderAddress.getHostAddress());
            // System.out.println("Porta do programa que enviou os dados multicast: " +
            // senderPort);

            // Crie um pacote de dados para enviar de volta ao remetente (ACK)
            byte sendData[] = Integer.toString(this.hashCode()).getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, senderAddress, senderPort);
            try {
                socket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInputStream ois;
            try {
                ois = new ObjectInputStream(bis);
                obj = (JSOUPData) ois.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            if (obj != null) {
                inserirURL(obj);
/*
                if (inserirURL(obj)) System.out.println("OBJETO COM URL INDEXADO: " + obj.getUrl());
*/
            }
            // indexar info recebida por multicast pelos downloaders
                /*infoURL urlAtual = null;
                String url = obj.getUrl();
                System.out.println("A indexar o objeto com url " + url);
                String title = obj.getTitle();
                String citation = obj.getCitation();
                HashSet<String> termos = obj.getTermos();
                HashSet<String> hip = obj.getHip();

                if (!urls.containsKey(url)) {
                    urlAtual = new infoURL(url, title, citation);
                    urls.put(url, urlAtual);
                } else {
                    urlAtual = urls.get(url);
                    urlAtual.setTitle(title);
                    urlAtual.setCitation(citation);
                }

                // adicionar cada termo encontrado no index se não existir, se existir apenas
                // adicionar o url como value
                for (String termoString : termos) {
                    if (!ind.containsKey(termoString)) {
                        // temos de criar entrada
                        HashSet<infoURL> urls = new HashSet<>();
                        urls.add(urlAtual);
                        ind.put(termoString, urls);
                    } else {
                        ind.get(termoString).add(urlAtual);
                    }
                }

                for (String hipString : hip) {
                    if (!urls.containsKey(hipString)) {
                        infoURL info = new infoURL(hipString);
                        info.addURL(urlAtual);
                        urls.put(hipString, info);
                    } else {
                        infoURL existente = urls.get(hipString);
                        existente.addURL(urlAtual);
                    }
                }

                // //salvar para disco
                // ConcurrentHashMap<?,?> copyInd = new ConcurrentHashMap<>(ind);
                // ConcurrentHashMap<?,?> copyUrls = new ConcurrentHashMap<>(urls);

                if (countIterations == 25) {
                new Thread(() -> {
                        //System.out.println("SALVAR DISCO");
                        lockFile.lock();
                        FileOps.writeToDisk(new File(String.format("./ISB/index_%s.bin", barrelName)), ind);
                        FileOps.writeToDisk(new File(String.format("./ISB/urls_%s.bin", barrelName)),  urls);
                        lockFile.unlock();
                    }).start();
                    countIterations = 0;
                }
                countIterations++;
            }

        }*/
        }
    }

    /**
     * HashMap personalizado, de forma a conseguir obter de forma rápida os urls que
     * aparecem x vezes nos termos, para aparecerem em primeiro lugar os urls que
     * tem mais correspondência
     */
    class searchTermosHash<infoURL, Integer> extends HashMap<infoURL, Integer> {
        HashMap<Integer, HashSet<infoURL>> reverse = new HashMap<>();

        @Override
        public Integer put(infoURL key, Integer value) {
            // verificar se ja existe tamanho inferior, se for o caso apagar
            int i = (int) value - 1;
            if (reverse.get(i) != null && reverse.get(i).contains(key))
                reverse.get(i).remove(key);

            reverse.computeIfAbsent(value, k -> new HashSet<infoURL>());
            reverse.get(value).add(key);

            return super.put(key, value);
        }

        public HashSet<infoURL> getKeys(Integer value) {
            return reverse.get(value);
        }

        public ArrayList<Integer> getAllKeys() {
            ArrayList<Integer> keys = new ArrayList<>(reverse.keySet());
            Collections.reverse(keys);
            return keys;
        }
    }

    @Override
    synchronized public ArrayList<infoURL> resultadoPesquisa(String termo_pesquisa, Integer id_client) throws RemoteException {

        server.makeBarrelUnavailable(this.barrelName);

        PreparedStatement s;
        ResultSet r;
        Connection c;
        String[] termos = termo_pesquisa.toLowerCase().split(" ");
        searchTermosHash<infoURL, Integer> urls = new searchTermosHash<>();
        //guarda para cada url o info url já recolhido
        HashMap<String,infoURL> aux = new HashMap<>();

        for (String t : termos) {
            try {
                c = makeConnection();
                c.setAutoCommit(false);
                c.isReadOnly();
                String sql = """
                    select url.url,titulo,citacao, count
                    from indice_invertido
                    left join url on indice_invertido.url_termo=url.url
                    left join  contagem_referencias on url.url=contagem_referencias.url
                    where termo = ?
                    order by count DESC;
                        """;
                s = c.prepareStatement(sql);
                s.setString(1, t);
                r = s.executeQuery();

                if (r.getMetaData().getColumnCount() == 4) {
                    while (r.next()) {
                        //obter url
                        String url = r.getString(1);
                        //obter titulo
                        String title = r.getString(2);
                        //obter citacao
                        String citation =r.getString(3);

                        Integer count_refs=r.getInt(4);

                        infoURL url_find;
                        if(aux.containsKey(url)){
                            url_find= aux.get(url);
                            aux.put(url,url_find);
                        }
                        else {
                            url_find=new infoURL(url,title,citation,count_refs);
                            aux.put(url,url_find);
                        }

                        if (urls.containsKey(url_find)) {
                            urls.put(url_find, urls.get(url_find) + 1);
                        } else {
                            urls.put(url_find, 1);
                        }
                    }
                }
                c.commit();
                s.close();
                c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        ArrayList<infoURL> sortedTermSearch = new ArrayList<>();

        for (Integer i : urls.getAllKeys()) {
            // System.out.println(i);
            HashSet<infoURL> termsSearch = urls.getKeys(i);
            ArrayList<infoURL> sortedTermsSearch_ = new ArrayList<>(termsSearch);
            // ordenar pelo numero de referencias
            sortedTermsSearch_.sort(Comparator.comparing(infoURL::getCount_refs));
            // System.out.println(sortedTermsSearch_);
            sortedTermSearch.addAll(sortedTermsSearch_);
        }

        // nao existem mais paginas
        if (sortedTermSearch.size() <= 10)
            sortedTermSearch.add(new infoURL("fim"));
        else {

            resultados_pesquisa.put(id_client,
                    new ArrayList<infoURL>(sortedTermSearch.subList(10, sortedTermSearch.size())));
            sortedTermSearch = new ArrayList<infoURL>(sortedTermSearch.subList(0, 10));
        }
        server.makeBarrelAvailable(this.barrelName);
        if (sortedTermSearch.size() > 0)
            return sortedTermSearch;
        return null;

      /*  // procurar pelo termo
        String[] termos = termo_pesquisa.split(" ");
        searchTermosHash<infoURL, Integer> urls = new searchTermosHash<>();

        for (String s : termos) {
            if (ind.containsKey(s.toLowerCase())) {
                for (infoURL i : ind.get(s.toLowerCase())) {

                    if (urls.containsKey(i)) {
                        urls.put(i, urls.get(i) + 1);
                    } else {
                        urls.put(i, 1);
                    }

                }
            }
        }

        ArrayList<infoURL> sortedTermSearch = new ArrayList<>();

        for (Integer i : urls.getAllKeys()) {
            // System.out.println(i);
            HashSet<infoURL> termsSearch = urls.getKeys(i);
            ArrayList<infoURL> sortedTermsSearch_ = new ArrayList<>(termsSearch);
            // ordenar pelo numero de referencias
            sortedTermsSearch_.sort(Comparator.comparing(infoURL::numeroURL));
            // System.out.println(sortedTermsSearch_);
            sortedTermSearch.addAll(sortedTermsSearch_);
        }

        // nao existem mais paginas
        if (sortedTermSearch.size() <= 10)
            sortedTermSearch.add(new infoURL("fim"));
        else {
            resultados_pesquisa.put(id_client,
                    new ArrayList<infoURL>(sortedTermSearch.subList(10, sortedTermSearch.size())));
            sortedTermSearch = new ArrayList<infoURL>(sortedTermSearch.subList(0, 10));
        }
        new Thread(() -> {
            // if (countIterations == 25) {
                lockFile1.lock();
                FileOps.writeToDisk(new File(String.format("./ISB/searchResults_%s.bin", barrelName)),(resultados_pesquisa));
                lockFile1.unlock();
                // countIterations = 0;
            // }
        }).start();*/

      /*  server.makeBarrelAvailable(this.barrelName);
        if (sortedTermSearch.size() > 0)
            return sortedTermSearch;
        return null;*/
    }

    @Override
    public ArrayList<infoURL> resultsReferencesList(String url) throws RemoteException {
        Connection c = makeConnection();
        PreparedStatement s;
        ResultSet r;
        ArrayList<infoURL> result = new ArrayList<>();

        try {
            c.setAutoCommit(false);
            c.isReadOnly();
            String sql= """
                        select url.url ,titulo,citacao
                        from url_referencia
                        join url on url_referencia.url_referencia  = url.url
                        where url_referencia.url = ?;
                       """;
            s=c.prepareStatement(sql);
            s.setString(1,url);
            r=s.executeQuery();

            while(r.next()){
                result.add(new infoURL(r.getString(1),r.getString(2),r.getString(3),-1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
      /*  if (urls.containsKey(url)) {
            return new ArrayList<>(this.urls.get(url).getUrls());
        } else
            return null;*/

        if (result.size()!=0) {
            return result;
        } else
            return null;
    }

    @Override
    synchronized public ArrayList<infoURL> continueSearch(Integer id_client) throws RemoteException {
        if (resultados_pesquisa.containsKey(id_client)) {
            ArrayList<infoURL> l = resultados_pesquisa.get(id_client);
            ArrayList<infoURL> res = new ArrayList<infoURL>(l.subList(0, Math.min(10, l.size())));
            if (l.size() > 10)
                resultados_pesquisa.put(id_client, new ArrayList<infoURL>(l.subList(Math.min(10, l.size()), l.size())));
            else {
                res.add(new infoURL("fim"));
                // remover id_client da pesquisa
                resultados_pesquisa.remove(id_client);
            }

            return res;
        }

        else
            return null;
    }

    @Override
    public String verify() throws RemoteException {
        return barrelName;
    }
}
