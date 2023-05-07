package com.ProjetoSD_META2.ProjetoSD_META2.Spring;

/*
import com.ProjetoSD_META2.ProjetoSD_META2.RMI;
*/
import com.ProjetoSD_META2.ProjetoSD_META2.Component;
import com.ProjetoSD_META2.ProjetoSD_META2.Searched;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.ProjetoSD_META2.ProjetoSD_META2.RMI;
import com.ProjetoSD_META2.ProjetoSD_META2.infoURL;

import javax.servlet.http.HttpSession;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import java.rmi.*;
import java.util.Objects;

@Controller
public class HomeController {

    RMI server;


    public HomeController() {

         System.out.println("A iniciar a conexao RMI ao Search Module");
         try {
             this.server = (RMI) Naming.lookup("rmi://localhost:3366/server");
         } catch (Exception e) {
             e.printStackTrace();
             System.exit(0);
         }

    }

    @GetMapping("/")
    public String home(HttpSession s, Model model){
        model.addAttribute("inptext",new InputText());

        return "home";
    }

    @PostMapping("/search")
    public String searchTermo(@ModelAttribute InputSearch in, Model model){

        ArrayList<infoURL> urls = null;
        try{
            urls = server.resultadoPesquisa(in.getInp(), 1);
        }catch (RemoteException e){
            e.printStackTrace();
        }

        model.addAttribute("search",in.getInp());

//        ArrayList<infoURL> urls = new ArrayList<>();
//        urls.add (new infoURL("url","title","citation"));
//        urls.add (new infoURL("url1","title1","citation1"));

        model.addAttribute("results", Objects.requireNonNullElse(urls, new infoURL("No results found!","","")));

        return "result_search";
    }

    @PostMapping("/index-new-url")
    public String indexarUrl( @ModelAttribute InputText in, Model model){


         String result;
         try {
             server.putURLClient(in.getInp());
             result = "URL indexada com sucesso";
         } catch (RemoteException e) {
             e.printStackTrace();
             result = "Erro ao indexar URL";
         }

        // System.out.println(in.getInp() + " " + result);
        model.addAttribute("inptext",new InputText());

        //se o url for indexado
        model.addAttribute("result","URL " + in.getInp() + " indexed!");

        return "index_url";
    }

    @GetMapping("/index-url")
    public String indexUrlPage(Model model){
        model.addAttribute("inptext",new InputText());
        return "index_url";
    }

    @PostMapping("/search-reference-urls")
    public String getReferenceUrl( @ModelAttribute InputText in, Model model){

        System.out.println("GET REFERENCES: "+in.getInp());

        ArrayList<infoURL> urls = null;
         try {
             urls = server.getReferencesList(in.getInp());
         } catch (RemoteException e) {
             e.printStackTrace();
         }

//        ArrayList<infoURL> urls = new ArrayList<>();
//        urls.add (new infoURL("url","title","citation"));
//        urls.add (new infoURL("url1","title1","citation1"));

        model.addAttribute("search", in.getInp());
        model.addAttribute("results", Objects.requireNonNullElse(urls, new infoURL("No results found!","","")));

        return "result_references";
    }

    @GetMapping("/reference-url")
    public String getReferenceUrlPage(Model model){
        model.addAttribute("inptext",new InputText());
        return "reference_urls";
    }

    @GetMapping("/stats-of-system")
    public String getStats(Model model){
       //get stats

        HashMap<String, Component> stats=null;

        try {
            stats=server.getComponents();
           model.addAttribute("stats",stats);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        ArrayList<Searched> searches=null;
        try {
            searches=server.getTopSearchs();
            model.addAttribute("searches",searches);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        return "admin_page";
    }

    @MessageMapping("/update-stats")
    @SendTo("/stats/messages")
    public StatsMessage onUpdate(StatsMessage m){
        System.out.println(m);
        return  m;
    }
}