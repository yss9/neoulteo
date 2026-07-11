package com.neoulteo.global.web;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {
    @GetMapping({"", "/"})
    @ResponseBody
    public Map<String, String> index() {
        return Map.of(
                "name", "Neoulteo API",
                "message", "Run the Vue frontend from the frontend directory.");
    }
}
