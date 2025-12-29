package aletheia.project.Aletheia.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import aletheia.project.Aletheia.entity.UserEntity;
import aletheia.project.Aletheia.repository.UserRepository;

@ControllerAdvice // This annotation makes it apply to ALL Controllers
public class GlobalControllerAdvice {

    @Autowired
    private UserRepository userRepository;

    // This method runs before every request
    @ModelAttribute
    public void addUserToModel(Model model, Principal principal) {
        // If the user is logged in (principal is not null)
        if (principal != null) {
            String email = principal.getName();
            // Fetch the user and add it to the model as "user"
            // This makes ${user} available in EVERY Thymeleaf template (sidebar, navbar, etc.)
            UserEntity user = userRepository.findByEmail(email).orElse(null);
            model.addAttribute("user", user);
        }
    }
}