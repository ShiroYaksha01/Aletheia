package aletheia.project.Aletheia.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import aletheia.project.Aletheia.dto.LoginRequest;
import aletheia.project.Aletheia.dto.PaperRequest;
import aletheia.project.Aletheia.dto.RegisterRequest;
import aletheia.project.Aletheia.entity.PaperEntity;
import aletheia.project.Aletheia.entity.UserEntity;
import aletheia.project.Aletheia.repository.PaperRepository;
import aletheia.project.Aletheia.repository.UserRepository;
import aletheia.project.Aletheia.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class WebController {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Autowired
    private PaperRepository paperRepository;
    
    @Autowired
    private UserRepository userRepository;

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
        return "auth/signup"; // points to src/main/resources/templates/auth/signup.html
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        // 1. Get the current user
        String email = principal.getName();
        UserEntity user = userRepository.findByEmail(email).orElseThrow();

        // 2. Fetch papers for this user
        List<PaperEntity> papers = paperRepository.findByAuthorId(user.getId());

        // 3. Calculate Stats
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", papers.size());
        stats.put("pending", (int) papers.stream().filter(p -> "PENDING".equalsIgnoreCase(p.getStatus())).count());
        stats.put("accepted", (int) papers.stream().filter(p -> "ACCEPTED".equalsIgnoreCase(p.getStatus())).count());
        stats.put("rejected", (int) papers.stream().filter(p -> "REJECTED".equalsIgnoreCase(p.getStatus())).count());

        // 4. Pass data to the view
        model.addAttribute("papers", papers); // The HTML iterates over this
        model.addAttribute("stats", stats);   // The HTML uses ${stats.total}, etc.
        
        return "dashboard/researcher";
    }

    @GetMapping("/paper-form")
    public String createPaperPage(Model model) {
        model.addAttribute("paperRequest", new PaperRequest());
        return "papers/submit";
    }
    
    @GetMapping("/reviews/list")
    public String reviewList() {
        return "pages/reviewlist";
    }
    
}
