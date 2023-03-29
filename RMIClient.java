import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class RMIClient {
    Scanner sc;
    RMI server;
    String loggedUser;
    Boolean logged;

    public RMIClient() {
        this.sc = new Scanner(System.in);
        try {
            this.server = (RMI) Naming.lookup("rmi://localhost:3366/server");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        logged=false;
    }

    public static void main(String[] args) {
        RMIClient c = new RMIClient();
        c.start();
    }

    public void start() {
        System.out.println("CLIENT A CORRER...");
        
        String input;
        Boolean flagWorked;

        while (true) {
            int opt = menu();
            flagWorked = false;

            switch (opt) {
                case 1: // INDEX NEW URL
                    input = sc.nextLine();

                    while (!flagWorked) {
                        try {
                            server.putURLClient(input);
                            flagWorked = true;
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case 2: // MAKE A SEARCH
                    input = sc.nextLine();
                    while (!flagWorked) {
                        try {
                            ArrayList<infoURL> result = server.resultadoPesquisa(input, this.hashCode());
                            int num_pagina = 1;
                            if (result != null) {
                                // se o tamanho for igual a 10, não há mais resultados
                                if (result.get(result.size() - 1).getUrl().equals("fim")) {
                                    System.out.println("PAGINA " + 1 + "\n");
                                    for (infoURL iUrl : result) {
                                        if (iUrl.getUrl().equals("fim"))
                                            break;
                                        System.out.println(iUrl);
                                    }

                                    System.out.println("SEM MAIS PAGINAS A MOSTRAR");
                                }

                                // se o tamanho for igual a 11, quer dizer que existem mais paginas
                                else if (result.size() >= 11) {
                                    Integer hash_barrel = -1;
                                    System.out.println("PAGINA " + 1 + "\n");
                                    for (infoURL iUrl : result) {
                                        if (result.indexOf(iUrl) == result.size() - 1)
                                            break;
                                        System.out.println(iUrl);
                                    }
                                    // o hash do barrel foi guardado na variavel url de forma a ser mais facil
                                    hash_barrel = Integer.parseInt(result.get(result.size() - 1).getUrl());

                                    String option_pages = " ";
                                    while (true) {
                                        result = null;
                                        option_pages = "";
                                        while (!option_pages.equals("q") && !option_pages.equals("n")) {
                                            System.out.println("PARA AVANCAR NA PAGINA: n , PARA SAIR DA PESQUISA q ");
                                            option_pages = sc.nextLine();
                                        }

                                        if (option_pages.equals("n")) {
                                            result = server.continueSearching(this.hashCode(), hash_barrel);
                                        }

                                        if (option_pages.equals("q"))
                                            break;

                                        if (result == null) {
                                            System.out.println("SEM MAIS PAGINAS A MOSTRAR");
                                            break;
                                        }

                                        else {
                                            num_pagina += 1;
                                            System.out.println("PAGINA " + num_pagina + "\n");
                                            for (infoURL iUrl : result) {
                                                if (iUrl.getUrl().equals("fim"))
                                                    break;
                                                System.out.println(iUrl);
                                            }

                                            if (result.get(result.size() - 1).getUrl().equals("fim")) {
                                                System.out.println("SEM MAIS PAGINAS A MOSTRAR");
                                                break;
                                            }
                                        }
                                    }
                                }
                            } else
                                System.out.println("SEM RESULTADOS");

                            flagWorked = true;
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case 3: // GET LIST OF PAGES THAT REFERENCE THE RECEIVED URL
                    if(!logged) {System.out.println("ACESSO RESERVADO: FAZER LOGIN OU REGISTO!"); break;}

                    input = sc.nextLine();

                    while (!flagWorked) {
                        try {
                            List<infoURL> result = server.getReferencesList(input);

                            if (result != null)
                                for (infoURL r : result) {
                                    System.out.println(r);
                                }
                            else
                                System.out.println("URL não indexado!!");
                            flagWorked = true;

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case 4: // STATUS2
                    if(!logged){System.out.println("ACESSO RESERVADO: FAZER LOGIN OU REGISTO!"); break;}
                    while (!flagWorked) {
                        try {
                            HashMap<String, Component> components = server.getComponents();
                            ArrayList<Searched> topSearchs = server.getTopSearchs();

                            Component currentComp;

                            for (String key : components.keySet()) {
                                currentComp = components.get(key);

                                if (Character.isDigit(key.charAt(1))) {
                                    // BARREL
                                    System.out.println(String.format("Type: Barrel | Hash: %s | IP: %s | Available: %b",
                                            key, currentComp.getIp(), currentComp.getIsAvailable()));

                                } else {
                                    // DOWNLOADER
                                    System.out.println(
                                            String.format("Type: Downloader | Name: %s | IP: %s | Available: %b", key,
                                                    currentComp.getIp(), currentComp.getIsAvailable()));

                                }
                            }

                            System.out.println(" -- Top Searches -- ");
                            for (Searched searchObj : topSearchs)
                                System.out.println(
                                        String.format("%d - %s", searchObj.getNumSearches(), searchObj.getTerm()));
                            System.out.println(" -------------------");

                            // System.out.println(result);
                            flagWorked = true;

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case 5:
                    // GET INTPUT
                    System.out.println("-- Making Login --");
                    System.out.println("Username: ");
                    String username = sc.nextLine();
                    System.out.println("Password: ");
                    String pw = sc.nextLine();

                    while (!flagWorked) {
                        try {
                            loggedUser = server.makeLogin(username, pw);
                            flagWorked = true;
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    if (loggedUser == null)
                        System.out.println("Erro. Username ou password inválidos.");
                    else{
                        System.out.println("Login efetuado com sucesso!");
                        logged=true;}
                    break;

                case 6:
                    // GET INTPUT
                    System.out.println("-- Registering a new user --");
                    System.out.println("Username: ");
                    String un = sc.nextLine();
                    System.out.println("Password: ");
                    String pass = sc.nextLine();

                    while (!flagWorked) {
                        try {
                            if (server.makeRegister(un, pass) == 0){
                                System.out.println("Utilizador registado com sucesso!");
                                logged=true;}
                            else
                                System.out.println("Erro. Utilizador já registado.");
                            flagWorked = true;
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    break;

                case 0: // CLOSE
                    System.exit(0);

                default: // ERROR
                    System.out.println("Invalid command: ");
            }
        }
    }

    private int menu() {
        System.out.println("1- Adicionar ULR");
        System.out.println("2- Pesquisar termo");
        System.out.println("3 - Lista de paginas com ligação a uma específica");
        System.out.println("4 - Estatísticas");
        System.out.println("5 - Login");
        System.out.println("6 - Registo");
        System.out.println("0 PARA SAIR");

        int opt = 0;
        Boolean flag = false;
        while (!flag) {
            try {
                opt = sc.nextInt();
                flag = true;
            } catch (InputMismatchException e) {
                System.out.println("Introduzir uma opcao com o numero!");
            }
            sc.nextLine();
        }

        return opt;
    }

}
