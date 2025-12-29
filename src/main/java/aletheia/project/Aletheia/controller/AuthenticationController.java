package aletheia.project.Aletheia.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import aletheia.project.Aletheia.dto.AuthResponse;
import aletheia.project.Aletheia.dto.LoginRequest;
import aletheia.project.Aletheia.dto.RegisterRequest;
import aletheia.project.Aletheia.entity.UserEntity;
import aletheia.project.Aletheia.repository.UserRepository;
import aletheia.project.Aletheia.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
public class AuthenticationController {

    private final PasswordEncoder encoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthenticationController(
            PasswordEncoder encoder,
            UserRepository userRepository,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService) {
        this.encoder = encoder;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }
    
// Login Page
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

    // Register Page
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/signup"; // points to src/main/resources/templates/auth/signup.html
    }   
    
    
    // Login
    @PostMapping("/login-process")
    public String login(@ModelAttribute LoginRequest loginRequest, HttpServletResponse response) {
        try {
                // 1. Authenticate the user
                authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
                );

                // 2. Generate the JWT
                String token = jwtUtil.generateToken(loginRequest.getEmail());

                // 3. Create a Cookie to store the JWT
                Cookie cookie = new Cookie("jwt", token);
                cookie.setHttpOnly(true); // Prevents JavaScript from stealing the token
                cookie.setPath("/");
                response.addCookie(cookie);

                return "redirect:/dashboard"; // Redirect to your home/dashboard page
        } catch (Exception e) {
                return "redirect:/login?error=true";
        }
    }

    // Register
    @PostMapping("/register-process")
    public String register(@ModelAttribute @Valid RegisterRequest registerRequest, HttpServletResponse response) {
        // 1. Check if user exists
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return "redirect:/register?error=user_exists";
        }

        // 2. Map DTO to Entity and Save
        UserEntity user = new UserEntity();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(encoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());

        String role = registerRequest.getRole();
        if (!"REVIEWER".equals(role)) {
            role = "RESEARCHER"; // Default to Researcher for safety
        }
        
        user.setRole(role);
        user.setActive(true);
        userRepository.save(user);

        // 3. Generate Token and Set Cookie
        String token = jwtUtil.generateToken(user.getEmail());
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(3600);
        response.addCookie(cookie);

        return "redirect:/dashboard"; // Redirect to your home/dashboard page
    }

}
