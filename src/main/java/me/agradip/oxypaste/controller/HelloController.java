package me.agradip.oxypaste.controller;

import me.agradip.oxypaste.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Autowired
    private AppConfig appConfig;

    @GetMapping("/hello")
    public String sayHello() {
        return "Hi bruh";
    }
}
