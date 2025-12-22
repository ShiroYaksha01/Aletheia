package aletheia.project.Aletheia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import aletheia.project.Aletheia.entity.UserEntity;
import aletheia.project.Aletheia.repository.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaperRepository paperRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // TODO: show system stats and overview
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        // TODO: list all users for management
        return "admin/users";
    }

    @GetMapping("/users/{id}/edit")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        // TODO: load user for editing
        return "admin/edit-user";
    }

    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable Long id, @ModelAttribute UserEntity userEntity, Model model) {
        // TODO: update user details
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id) {
        // TODO: activate/deactivate user
        return "redirect:/admin/users";
    }
}