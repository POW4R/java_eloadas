package hu.zkga6i.marketapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("appName", "MarketApp");
        model.addAttribute("description", "Professional Currency and Forex Trading Platform");
        return "index";
    }
}