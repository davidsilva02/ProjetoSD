package com.ProjetoSD_META2.ProjetoSD_META2.Spring;

/*
import com.ProjetoSD_META2.ProjetoSD_META2.RMI;
*/
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
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
import javax.websocket.Session;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import java.rmi.*;
import java.util.Objects;

import java.io.*;
import java.net.*;

class Request{

    public static String getRequestStr(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {

                for (String line; (line = reader.readLine()) != null; ) {
                    result.append(line);
                }
        }
        return result.toString();

    }

    public static JSONObject getRequestJson(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {

            JSONTokener tokener = new JSONTokener(reader);

//            System.out.println("tokener " + tokener.nextValue());


            if( tokener.next() != '{' )
                return null;
            else {
                tokener.back();
//                System.out.println("tokener " + tokener.nextValue());
                return new JSONObject(tokener);
            }
        }

    }

}

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
    public String searchTermo(@ModelAttribute InputSearch in, Model model, HttpSession s){
        if(in.getInp().equals("")) return "redirect:/";
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
    public String getStats(Model model,HttpSession s){
       //get stats
        if(s.getAttribute("token")==null) return "redirect:/login";

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

    @GetMapping("/login")
    public String login(Model model){
        model.addAttribute("loginInput",new LoginInput());
        return "login";
    }

    @GetMapping("/register")
    public String register (Model model){
        model.addAttribute("loginInput",new LoginInput());
        return "register";
    }

    @PostMapping("/make-login")
    public String makeLogin(@ModelAttribute LoginInput login, HttpSession s,Model model){
        System.out.println(login);
        String user=null;
        try {
            user=server.makeLogin(login.getUser(),login.getPassword());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        if(user==null) model.addAttribute("result","USER AND PASSWORD NOT FOUND!");
        if(user!=null) model.addAttribute("result",String.format("WELCOME, @%s",user));
        s.setAttribute("token",user);
        return "result";
    }
    @PostMapping("/make-register")
    public String makeRegister(@ModelAttribute LoginInput login, HttpSession s){
        System.out.println(login);

        //register user and password
        try {
            server.makeRegister(login.getUser(),login.getPassword());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }


        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession s){
        //remove token
        s.removeAttribute("token");
        return "redirect:/";
    }
    @GetMapping("/index-hackernews-stories")
    public String indexHackerNewsStories(@RequestParam("termos") String termosPesquisa, Model model) throws Exception {

        System.out.println("HACKER NEWS");

        //TODO ISTO TEM DE SER REVISTO DE ACORDO COMO AS COISAS VÊM PELO THYMELEAF E ASSIM, ESTÁ APENAS POR REPRESENTATIVIDADE
/**/
        System.out.println(termosPesquisa);

        String strTopStoriesId = null;
        try {
            strTopStoriesId = Request.getRequestStr("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<Integer> arrayTopStories = null;
        try {
            arrayTopStories = new ObjectMapper().reader(List.class).readValue(strTopStoriesId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        //DEBUG
        System.out.println("Looping through stories...");
        Boolean indexed = false;

        for(Integer storyId: arrayTopStories){
            //ir buscar a top story, verificar o texto e, se sim, indexar

            String url = "https://hacker-news.firebaseio.com/v0/item/" + storyId + ".json?print=pretty";
//            System.out.println(url);
            JSONObject story = Request.getRequestJson(url);

//            System.out.println(story);

            //String texto = (String) story.get("text");
            //TODO COMO É QUE SE VAI BUSCAR O TEXTO???
            String texto = (String) story.get("title");


            if(texto.contains(termosPesquisa)){

                //DEBUG
                System.out.println("Indexing story url: " + url);

                server.putURLClient((String)story.get("url"));

                indexed = true;
            }
        }

        //flag para ver se foi indexada alguma coisa
        if(indexed)
            model.addAttribute("result_index", "Success!");
        else
            model.addAttribute("result_index", "Terms not found in HackerNews Top Stories!");

        return "index_hackernews";
    }

@GetMapping("/index-hackernews-username-page")
    public String indexHackerRankUsernamePage(Model model){
        model.addAttribute("inptext",new InputText());
        return "index_hackernews_username_page";
    }

    @PostMapping("/index-hackernews-username")
    public String indexHackerNewsUsername(InputText in, Model model){

        String username = in.getInp();
        String url = "https://hacker-news.firebaseio.com/v0/user/" + username + ".json?print=pretty";

        //DEBUG
        System.out.println("Indexing " + username + "'s stories...");

        try {
            JSONObject userdata = Request.getRequestJson(url);

            if( userdata == null){
                model.addAttribute("result_index", "Username not found!");

                return "index_hackernews";
            }

            JSONArray arr = (JSONArray) userdata.get("submitted");

//            System.out.println("teste: ");
//            for(Object obj: arr)
//                System.out.println(obj);

            // iterate over user's stories and index them
            for (Object story: arr) {

                Integer currStory = Integer.parseInt( story.toString() );
                String newUrl = "https://hacker-news.firebaseio.com/v0/item/" + currStory + ".json?print=pretty";

                //get story info
                JSONObject storyInfo = Request.getRequestJson(url);

                if(storyInfo != null) {
                    //DEBUG
                    System.out.println("Indexing " + username + "'s story: " + (String) storyInfo.get("url"));

                    server.putURLClient( (String) storyInfo.get("url") );
                }
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        model.addAttribute("result_index", "Success");

        return "index_hackernews";
    }



}