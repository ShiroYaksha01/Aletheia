package aletheia.project.Aletheia.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import aletheia.project.Aletheia.entity.PaperEntity;
import aletheia.project.Aletheia.entity.UserEntity;
import aletheia.project.Aletheia.repository.PaperRepository;
import aletheia.project.Aletheia.repository.ReviewRepository;
import aletheia.project.Aletheia.repository.UserRepository;



@Controller
public class WebController {


    @Autowired
    private PaperRepository paperRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        // 1. Get the current user
        String email = principal.getName();
        UserEntity user = userRepository.findByEmail(email).orElseThrow();

        // 2. Route based on role
        String role = user.getRole();
        
        if ("ADMIN".equalsIgnoreCase(role)) {
            model.addAttribute("pageTitle", "Admin Dashboard");
            model.addAttribute("pageSubtitle", "System administration and user management");
            model.addAttribute("totalPapers", paperRepository.count());
            model.addAttribute("totalUsers", userRepository.count());
            model.addAttribute("totalReviews", reviewRepository.count());
            return "dashboard/admin";
        } else if ("REVIEWER".equalsIgnoreCase(role)) {
            model.addAttribute("pageTitle", "Reviewer Dashboard");
            model.addAttribute("pageSubtitle", "Review assigned papers");
            return "dashboard/reviewer";
        } else {
            // Default to RESEARCHER
            // Fetch papers for this user
            List<PaperEntity> papers = paperRepository.findByAuthorId(user.getId());

            // Calculate Stats
            Map<String, Integer> stats = new HashMap<>();
            stats.put("total", papers.size());
            stats.put("pending", (int) papers.stream().filter(p -> "PENDING".equalsIgnoreCase(p.getStatus())).count());
            stats.put("accepted", (int) papers.stream().filter(p -> "ACCEPTED".equalsIgnoreCase(p.getStatus())).count());
            stats.put("rejected", (int) papers.stream().filter(p -> "REJECTED".equalsIgnoreCase(p.getStatus())).count());

            // Pass data to the view
            model.addAttribute("papers", papers);
            model.addAttribute("stats", stats);
            
            model.addAttribute("pageTitle", "Dashboard");
            model.addAttribute("pageSubtitle", "Overview of your research activities");
            return "dashboard/researcher";
        }
    }


    
    
}
