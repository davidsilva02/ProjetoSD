package com.ProjetoSD_META2.ProjetoSD_META2.Spring;

import com.ProjetoSD_META2.ProjetoSD_META2.RMI;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.rmi.Naming;
@Controller
public class WebsocketController {

    public WebsocketController() {

        System.out.println("WebsocketController - A iniciar...");


    }

    @MessageMapping("/update-stats")
    @SendTo("/stats/messages")
    public StatsMessage onUpdate(StatsMessage m){
        //System.out.println(m);
        System.out.println("UPDATE STATS!");
        return  m;
    }
}
