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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.ProjetoSD_META2.ProjetoSD_META2.RMI;
import com.ProjetoSD_META2.ProjetoSD_META2.infoURL;

import javax.servlet.http.HttpSession;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.*;

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

         System.out.println("HomeController - A iniciar a conexao RMI ao Search Module");
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
    public String searchTermo(@ModelAttribute InputText in, Model model, HttpSession s){
        if(in.getInp().equals("")) return "redirect:/";

        ArrayList<infoURL> urls = null;
        int hash = -1;
        String userId = UUID.randomUUID().toString();;

        try{
            urls = server.resultadoPesquisa(in.getInp(), userId);

            if( urls.size() >= 11 && !urls.get(urls.size() - 1).getUrl().equals("fim")) {
                // o hash do barrel foi guardado na variavel url de forma a ser mais facil
                hash = Integer.parseInt(urls.get(urls.size() - 1).getUrl());
            }


        }catch (RemoteException e){
            e.printStackTrace();
        }

        model.addAttribute("search",in.getInp());
        model.addAttribute("results", Objects.requireNonNullElse(urls, new infoURL("No results found!","","")));
        model.addAttribute("page",1);
        model.addAttribute("barrelHash",hash);
        model.addAttribute("userId",userId);

        return "result_search";
    }

    @GetMapping("/search")
    public String searchTermoPage(@RequestParam("search") String search, @RequestParam("page") int pageNum, @RequestParam("hash") String hash, @RequestParam("userId") String userId, Model model, HttpSession s){

        ArrayList<infoURL> urls = null;

        int newHash = Integer.parseInt(hash);

        try{
            urls = server.continueSearching(userId, newHash);

            if( urls.get(urls.size() - 1).getUrl().equals("fim")) {
                // atualizar o hash se nao existirem mais resultados
                newHash = -1;
            }

        }catch (RemoteException e){
            e.printStackTrace();
        }


        model.addAttribute("search",search);
        model.addAttribute("page", pageNum+1);
        model.addAttribute("barrelHash",newHash);
        model.addAttribute("userId",userId);
        model.addAttribute("results", Objects.requireNonNullElse(urls, new infoURL("No results found!","","")));

        return "result_search";
    }

    @PostMapping("/index-new-url")
    public String indexarUrl( @ModelAttribute InputText in, Model model){


        // System.out.println(in.getInp() + " " + result);
        model.addAttribute("inptext",new InputText());

        //se o url for indexado
        model.addAttribute("result","URL " + in.getInp() + " indexed!");

        String result;
        try {
            server.putURLClient(in.getInp());
            result = "URL indexada com sucesso";
        } catch (RemoteException e) {
            e.printStackTrace();
            result = "Erro ao indexar URL";
        }

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


    @GetMapping("/index-hackernews-stories")
    public String indexHackerNewsStories(@RequestParam("termos") String termosPesquisa, Model model) {

        System.out.println("Searching HackerNews Top Stories. Searched Terms: " + termosPesquisa);

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
            JSONObject story = null;
            try {
                story = Request.getRequestJson(url);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //TODO COMO É QUE SE VAI BUSCAR O TEXTO???
            String texto = (String) story.get("title");


            if(texto.contains(termosPesquisa)){

                //DEBUG
                System.out.println("Indexing story url: " + url);

                try {
                    server.putURLClient((String)story.get("url"));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

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
        Integer indexed = 0;
        boolean flagErrors = false;

        //DEBUG
        System.out.println("Indexing " + username + "'s stories...");

        try {
            JSONObject userdata = Request.getRequestJson(url);

            if( userdata == null){
                model.addAttribute("result_index", "Username not found!");

                return "index_hackernews";
            }

            JSONArray arr = (JSONArray) userdata.get("submitted");
            System.out.println(arr);

            // iterate over user's stories and index them
            for (Object story: arr) {

                Integer currStory = Integer.parseInt( story.toString() );
                String newUrl = "https://hacker-news.firebaseio.com/v0/item/" + currStory + ".json?print=pretty";

                //get story info
                JSONObject storyInfo = Request.getRequestJson(newUrl);


                if(storyInfo != null) {

                    System.out.println("storyInfo: " + storyInfo + " : "+ newUrl);

                    //DEBUG
                    try{
                        if( ((String)storyInfo.get("type")).equals("story") ) {
                            System.out.println("Indexing " + username + "'s story: " + (String) storyInfo.get("url"));
                            System.out.println("Indexing2 " + username + "'s story: " + (String) storyInfo.get("url"));

                            server.putURLClient((String) storyInfo.get("url"));
                            indexed++;
                        }
                    }
                    catch (Exception e){
                        flagErrors = true;
                        e.printStackTrace();
                    }
                }
            }



        } catch (Exception e) {
            flagErrors = true;
            e.printStackTrace();
        }


        if( flagErrors  && indexed > 0)
            //ocorram erros e não foram indexadas nenhumas stories
            model.addAttribute("result_index", String.format("Error: Some type of error occured but %d stories were indexed",indexed));

        else if(flagErrors && indexed == 0)
            model.addAttribute("result_index", "Error: No stories indexed");

        else
            model.addAttribute("result_index", String.format("Success! %d stories indexed",indexed));

        return "index_hackernews";
    }




}