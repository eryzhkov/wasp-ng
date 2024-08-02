package ru.vsu.uic.wasp.ng.core.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Slf4j
public class DefaultController {

    @GetMapping("/")
    public String index() {
        log.info("DefaultController: index()");
        return "default-view";
    }

    @GetMapping("/login")
    public ModelAndView login() {
        log.info("DefaultController: login()");
        return new ModelAndView("login-view");
    }

    @PostMapping("/login-failed")
    public ModelAndView loginFailed() {
        log.info("DefaultController: loginFailed()");
        return new ModelAndView("login-failed-view");
    }

    @PostMapping("/home")
    public ModelAndView home() {
        log.info("DefaultController: home()");
        return new ModelAndView("home-view");
    }
}
