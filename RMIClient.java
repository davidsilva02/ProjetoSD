import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.util.List;
import java.util.Scanner;

public class RMIClient {
    Scanner sc;
    RMI server;
    String loggedUser;

    public RMIClient(){
        this.sc=new Scanner(System.in);
        try {
            this.server= (RMI) Naming.lookup("rmi://localhost:1099/server");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        loggedUser = null;
    }
    public static void main(String[] args) {
        RMIClient c = new RMIClient();
        c.start();
    }

    public void start(){
        System.out.println("CLIENT A CORRER...");

        String input;
        while(true){
           int opt=menu();

           switch(opt){
            case 1: // INDEX NEW URL
                input = sc.nextLine();
                try {
                    server.putURLClient(input);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            //TODO: AINDA NAO ESTA COMPLETO FALTA AQUILO DAS PAGINAS
            case 2: // MAKE A SEARCH
                input = sc.nextLine();
               try {
                   List<infoURL> result=server.resultadoPesquisa(input);
                   if(result!=null){
                       for( infoURL iUrl: result)
                            System.out.println(iUrl);
                   }
                   else System.out.println("SEM RESULTADOS");

               } catch (RemoteException e) {
                   e.printStackTrace();
               }
               break;

            case 5: // GET LIST OF PAGES THAT REFERENCE THE RECEIVED URL
                input = sc.nextLine();
              try {
                  List<infoURL> result= server.getReferencesList(input);
                  System.out.println(result);
                  
              } catch (Exception e) {
                  e.printStackTrace();
              }
              break;

           case 7:
              //GET INTPUT
              System.out.println("-- Making Login --");
              System.out.println("Username: ");
              String username = sc.nextLine();
              System.out.println("Password: ");
              String pw = sc.nextLine();

                try {
                    loggedUser = server.makeLogin(username, pw);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

              if(loggedUser == null)
                System.out.println("Erro. Username ou password inválidos.");
              else
                System.out.println("Login efetuado com sucesso!");
                break;

            case 8:
                //GET INTPUT
                System.out.println("-- Registering a new user --");
                System.out.println("Username: ");
                String un = sc.nextLine();
                System.out.println("Password: ");
                String pass = sc.nextLine();

                try {
                    if( server.makeRegister(un, pass) == 0 )
                        System.out.println("Utilizador registado com sucesso!");
                    else
                        System.out.println("Erro. Utilizador já registado.");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

               break;

           case 0: // CLOSE
            System.exit(0);
           
            default: //ERROR
               System.out.println("Invalid command: ");
            }
        }
    }



    private int menu(){
       System.out.println("1- Adicionar ULR");
       System.out.println("2- Pesquisar termo");
       System.out.println("5 - Lista de paginas com ligação a uuma específica");
       System.out.println("7 - Login");
       System.out.println("8 - Registo");
       System.out.println("0 PARA SAIR");

       int opt=0;

       opt=sc.nextInt();
       sc.nextLine();

       return opt;
    }

    
}
