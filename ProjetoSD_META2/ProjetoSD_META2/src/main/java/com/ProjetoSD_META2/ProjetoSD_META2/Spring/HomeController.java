package com.ProjetoSD_META2.ProjetoSD_META2.Spring;

/*
import com.ProjetoSD_META2.ProjetoSD_META2.RMI;
*/
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.ProjetoSD_META2.ProjetoSD_META2.RMI;

import java.rmi.Naming;
import java.rmi.RemoteException;

@Controller
public class HomeController {

    RMI server;


    public HomeController() {

        // System.out.println("A iniciar a conexao RMI ao Search Module");
        // try {
        //     this.server = (RMI) Naming.lookup("rmi://localhost:3366/server");
        // } catch (Exception e) {
        //     e.printStackTrace();
        //     System.exit(0);
        // }


    }

    @GetMapping("/")
    public String home(Model model){
        model.addAttribute("inptext",new InputText());
        return "home";
    }
    
    @PostMapping("/search")
    public String searchTermo( @ModelAttribute InputText in, Model model){
        System.out.println(in.getInp());
        model.addAttribute("inptext",in);
        return "result";
    }
    
    @PostMapping("/indexNewUrl")
    public String indexarUrl( @ModelAttribute InputText in, Model model){


        // String result;
        // try {
        //     server.putURLClient(in.getInp());
        //     result = "URL indexada com sucesso";
        // } catch (RemoteException e) {
        //     e.printStackTrace();
        //     result = "Erro ao indexar URL";
        // }

        // System.out.println(in.getInp() + " " + result);
        // model.addAttribute("inptext",result);

        System.out.println(in.getInp());
        model.addAttribute("inptext",in);
        
        return "home";
        // return "indexUrlResult";
    }

    @GetMapping("/indexUrlPage")
    public String indexUrlPage(Model model){
        model.addAttribute("inptext",new InputText());
        return "index_url";
    }
    
    
}