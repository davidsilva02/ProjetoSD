package com.ProjetoSD_META2.ProjetoSD_META2;

import java.io.*;
import java.net.*;

public class tryHttp {

    public static String getHTML(String urlToRead) throws Exception {
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

    public static void main(String[] args) throws Exception
    {
        System.out.println(getHTML("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty"));
    }
}