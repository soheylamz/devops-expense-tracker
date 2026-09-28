package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    // Dieser Endpunkt ist unter GET http://localhost:8080/api/hello erreichbar
    @GetMapping("/hello")
    public String sayHello() {
        return "Dev22Ops Pipeline läuft erfolgreich!";
    }
    private void demoFehler() {
        String wert = null;
        System.out.println(wert.length());
    }
}
