package aletheia.project.Aletheia.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import aletheia.project.Aletheia.entity.UserEntity;
import aletheia.project.Aletheia.repository.UserRepository;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String profile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        String email = principal.getName();
        UserEntity user = userRepository.findByEmail(email).orElseThrow();
        
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Edit Profile");
        model.addAttribute("pageSubtitle", "Update your personal information");
        
        return "profile/edit";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute UserEntity userForm, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        String currentEmail = principal.getName();
        UserEntity existingUser = userRepository.findByEmail(currentEmail).orElseThrow();
        
        // Check if email is being changed
        String newEmail = userForm.getEmail();
        if (!currentEmail.equals(newEmail)) {
            // Check if new email already exists
            if (userRepository.findByEmail(newEmail).isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email already exists in the system!");
                return "redirect:/profile";
            }
        }
        
        // Update allowed fields
        existingUser.setEmail(newEmail);
        existingUser.setFirstName(userForm.getFirstName());
        existingUser.setLastName(userForm.getLastName());
        
        // Only allow role change between RESEARCHER and REVIEWER (not ADMIN)
        String newRole = userForm.getRole();
        if ("RESEARCHER".equalsIgnoreCase(newRole) || "REVIEWER".equalsIgnoreCase(newRole)) {
            existingUser.setRole(newRole.toUpperCase());
        }
        
        userRepository.save(existingUser);
        
        // If email changed, user needs to log in again with new email
        if (!currentEmail.equals(newEmail)) {
            redirectAttributes.addFlashAttribute("successMessage", 
                "Profile updated! Please log in again with your new email address.");
            return "redirect:/logout";
        }
        
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/profile";
    }
}