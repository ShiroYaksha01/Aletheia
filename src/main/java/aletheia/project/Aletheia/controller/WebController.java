package aletheia.project.Aletheia.controller;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import aletheia.project.Aletheia.dto.LoginRequest;
import aletheia.project.Aletheia.dto.PaperRequest;
import aletheia.project.Aletheia.dto.RegisterRequest;
import aletheia.project.Aletheia.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class WebController {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public WebController(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/login")
    public String loginPage(Model model, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("jwt")) {
                    String token = cookie.getValue();

                    try {
                        // 1. Extract username from token
                        String username = jwtUtil.extractUsername(token);

                        // 2. Load the user
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        // 3. Validate token with user details
                        if (jwtUtil.validateToken(token, userDetails)) {
                            return "redirect:/dashboard"; // Already logged in
                        }
                    } catch (Exception e) {
                        // Token invalid or user not found, allow login page
                        break;
                    }
                }
            }
        }

        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/login";
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
    public String createPaperPage(Model model) {
        model.addAttribute("paperRequest", new PaperRequest());
        return "pages/createpaper";
    }
    
    @GetMapping("/reviews/list")
    public String reviewList() {
        return "pages/reviewlist";
    }
    
}
