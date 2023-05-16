package com.ProjetoSD_META2.ProjetoSD_META2.Spring;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.ProjetoSD_META2.ProjetoSD_META2.RMI;

import javax.servlet.http.HttpSession;
import java.rmi.Naming;
import java.rmi.RemoteException;


@Controller
public class UserController {
    RMI server;


    public UserController() {

        System.out.println("UserController - A iniciar a conexao RMI ao Search Module");
        try {
            this.server = (RMI) Naming.lookup("rmi://localhost:3366/server");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

    }

    @GetMapping("/login")
    public String login(Model model){
        model.addAttribute("loginInput",new CredentialsInput());
        return "login";
    }

    @GetMapping("/register")
    public String register (Model model){
        model.addAttribute("loginInput",new CredentialsInput());
        return "register";
    }

    @PostMapping("/make-login")
    public String makeLogin(@ModelAttribute CredentialsInput login, HttpSession s, Model model){
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
    public String makeRegister(@ModelAttribute CredentialsInput login, HttpSession s){
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

}
