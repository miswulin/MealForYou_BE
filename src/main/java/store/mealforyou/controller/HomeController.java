package store.mealforyou.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // 루트(/)로 들어오면 static/index.html로 포워딩
    @GetMapping("/")
    public String root() {
        return "forward:/index.html";
    }

    // /health도 같은 페이지로 리턴
    @GetMapping("/health")
    public String health() {
        return "forward:/index.html";
    }
}
