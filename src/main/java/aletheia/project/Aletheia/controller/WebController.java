package aletheia.project.Aletheia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import aletheia.project.Aletheia.dto.LoginRequest;
import aletheia.project.Aletheia.dto.PaperRequest;
import aletheia.project.Aletheia.dto.RegisterRequest;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class WebController {
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/login"; // points to src/main/resources/templates/auth/login.html
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/signup"; // points to src/main/resources/templates/auth/register.html
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "pages/dashboard";
    }

    @GetMapping("/paper-form")
    public String createPaper(Model model) {
        model.addAttribute("paperRequest", new PaperRequest());
        return "pages/createpaper";
    }
    
}
