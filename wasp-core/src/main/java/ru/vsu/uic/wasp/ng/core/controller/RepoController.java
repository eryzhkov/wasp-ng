package ru.vsu.uic.wasp.ng.core.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/repos")
public class RepoController {

    @GetMapping("")
    public ModelAndView index() {
        return new ModelAndView("repos/repos-view");
    }
}
