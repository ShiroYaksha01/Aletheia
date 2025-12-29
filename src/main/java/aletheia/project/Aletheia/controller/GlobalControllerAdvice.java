package aletheia.project.Aletheia.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import aletheia.project.Aletheia.entity.UserEntity;
import aletheia.project.Aletheia.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute
    public void addGlobalAttributes(Model model, Principal principal, HttpServletRequest request) {

        model.addAttribute("currentPath", request.getRequestURI());

        if (principal != null) {
            String email = principal.getName();
            UserEntity user = userRepository.findByEmail(email).orElse(null);
            
            if (user != null) {
                // 1. Add the User object (for names, email, etc.)
                model.addAttribute("user", user);

                // 2. Add Boolean Flags for Roles (Fixes your "Check Condition" issue)
                // This checks the String 'role' safely in Java
                String role = user.getRole();
                model.addAttribute("isResearcher", "RESEARCHER".equals(role));
                model.addAttribute("isReviewer", "REVIEWER".equals(role));
                model.addAttribute("isAdmin", "ADMIN".equals(role));
            }
        }
    }
}