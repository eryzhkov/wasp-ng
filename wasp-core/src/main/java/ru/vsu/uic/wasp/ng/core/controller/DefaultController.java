package ru.vsu.uic.wasp.ng.core.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DefaultController {

    @GetMapping("/")
    public String index() {
        return "default-view";
    }

    @GetMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("login-view");
    }

    @GetMapping("/login-failed")
    public ModelAndView loginFailed() {
        return new ModelAndView("login-failed-view");
    }

    @GetMapping("/home")
    public ModelAndView home() {
        return new ModelAndView("home-view");
    }
}
