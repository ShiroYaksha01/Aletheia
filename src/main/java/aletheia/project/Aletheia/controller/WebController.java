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
import aletheia.project.Aletheia.repository.UserRepository;



@Controller
public class WebController {


    @Autowired
    private PaperRepository paperRepository;
    
    @Autowired
    private UserRepository userRepository;


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
        
        // BreadCrumb
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("pageSubtitle", "Overview of your research activities");
        return "dashboard/researcher";
    }


    
    
}
