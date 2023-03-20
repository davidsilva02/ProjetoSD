import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

public class RMIClient {
    Scanner sc;
    RMI server;

    public RMIClient(){
        this.sc=new Scanner(System.in);
        try {
            this.server= (RMI) Naming.lookup("rmi://localhost:1099/server");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
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
                    server.putUrl(input);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;

            case 2: // MAKE A SEARCH
                input = sc.nextLine();
               try {
                   String result=server.resultadoPesquisa(input);
                   System.out.println(result);
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
       System.out.println("0 PARA SAIR");

       int opt=0;

       opt=sc.nextInt();
       sc.nextLine();

       return opt;
    }

    
}
