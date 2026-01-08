package aletheia.project.Aletheia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import aletheia.project.Aletheia.entity.PaperEntity;
import aletheia.project.Aletheia.entity.ReviewEntity;
import aletheia.project.Aletheia.entity.UserEntity;
import aletheia.project.Aletheia.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final PaperRepository paperRepository;
    private final ReviewRepository reviewRepository;

    // Constructor Injection (Best Practice)
    public AdminController(UserRepository userRepository, PaperRepository paperRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.paperRepository = paperRepository;
        this.reviewRepository = reviewRepository;
    }
    
    // === ASSIGN REVIEWERS LOGIC ===

    // 1. Show the Assign Page
    @GetMapping("/papers/{id}/assign")
    public String showAssignPage(@PathVariable Long id, Model model) {
        // A. Fetch the Paper
        PaperEntity paper = paperRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paper not found"));

        // B. Get Currently Assigned Reviews
        List<ReviewEntity> currentReviews = reviewRepository.findByPaperIdWithReviewer(id);
        
        // C. Get All Users with role "REVIEWER"
        // Note: Ensure your DB role is "REVIEWER" (no prefix)
        List<UserEntity> allReviewers = userRepository.findByRole("REVIEWER");

        // D. Filter: Create a list of Available Reviewers (Not author, not already assigned)
        List<Long> assignedIds = currentReviews.stream()
                .map(r -> r.getReviewer().getId())
                .collect(Collectors.toList());

        List<UserEntity> availableReviewers = allReviewers.stream()
                .filter(u -> !u.getId().equals(paper.getAuthor().getId())) // Cannot be the author
                .filter(u -> !assignedIds.contains(u.getId()))             // Cannot be already assigned
                .collect(Collectors.toList());

        // E. Calculate Workload (Active Reviews count) for badges
        Map<Long, Long> workloadMap = availableReviewers.stream()
                .collect(Collectors.toMap(
                        UserEntity::getId,
                        u -> reviewRepository.countActiveReviews(u.getId())
                ));

        // F. Add everything to Model
        model.addAttribute("paper", paper);
        model.addAttribute("currentReviews", currentReviews);
        model.addAttribute("availableReviewers", availableReviewers);
        model.addAttribute("workloadMap", workloadMap);

        return "admin/assign-reviewer"; // Maps to templates/admin/assign-reviewer.html
    }

    // 2. Process Assignment Form
    @PostMapping("/papers/{id}/assign")
    public String assignReviewer(@PathVariable Long id,
                                 @RequestParam Long reviewerId,
                                 @RequestParam LocalDate deadline,
                                 RedirectAttributes redirectAttributes) {
        try {
            PaperEntity paper = paperRepository.findById(id).orElseThrow();
            UserEntity reviewer = userRepository.findById(reviewerId).orElseThrow();

            // Create new Review Entity
            ReviewEntity review = new ReviewEntity();
            review.setPaper(paper);
            review.setReviewer(reviewer);
            review.setDeadline(deadline);
            review.setStatus("PENDING"); 
            // Score and Feedback are null initially
            
            reviewRepository.save(review);
            
            // Update Paper Status to UNDER_REVIEW if it was PENDING
            if ("PENDING".equalsIgnoreCase(paper.getStatus())) {
                paper.setStatus("UNDER_REVIEW");
                paperRepository.save(paper);
            }

            redirectAttributes.addFlashAttribute("success", "Reviewer assigned successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to assign reviewer.");
        }
        return "redirect:/admin/papers/" + id + "/assign";
    }

    // 3. Unassign (Remove) Reviewer
    @PostMapping("/papers/{id}/unassign")
    public String unassignReviewer(@PathVariable Long id, 
                                   @RequestParam Long reviewId, 
                                   RedirectAttributes redirectAttributes) {
        try {
            reviewRepository.deleteById(reviewId);
            redirectAttributes.addFlashAttribute("success", "Reviewer removed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not remove reviewer.");
        }
        return "redirect:/admin/papers/" + id + "/assign";
    }

    // === USER MANAGEMENT (From your stub) ===
    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    // Show edit user form
    @GetMapping("/users/{id}/edit")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("user", user);
        model.addAttribute("availableRoles", new String[]{"ADMIN", "RESEARCHER", "REVIEWER"});
        
        return "admin/edit-user";
    }

    // Process edit user form
    @PostMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id,
                          @RequestParam String firstName,
                          @RequestParam String lastName,
                          @RequestParam String email,
                          @RequestParam String role,
                          @RequestParam(defaultValue = "true") Boolean active,
                          RedirectAttributes redirectAttributes) {
        try {
            UserEntity user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Validate role
            if (!role.equals("ADMIN") && !role.equals("RESEARCHER") && !role.equals("REVIEWER")) {
                redirectAttributes.addFlashAttribute("error", "Invalid role selected.");
                return "redirect:/admin/users/" + id + "/edit";
            }
            
            // Check if email is unique (excluding current user)
            if (!user.getEmail().equals(email) && userRepository.findByEmail(email).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Email already exists.");
                return "redirect:/admin/users/" + id + "/edit";
            }
            
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setRole(role);
            user.setActive(active);
            
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("success", "User updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to update user.");
        }
        
        return "redirect:/admin/users";
    }

    //  === PAPER MANAGEMENT WITH FILTERS ===
    @GetMapping("/papers")
    public String managePapers(@RequestParam(value = "search", required = false) String search,
                               @RequestParam(value = "status", required = false, defaultValue = "all") String status,
                               Model model) {
        
        // 1. Fetch All Papers
        List<PaperEntity> papers = paperRepository.findAll();

        // 2. Filter (Java Stream approach for simplicity)
        if (search != null && !search.isEmpty()) {
            papers = papers.stream()
                    .filter(p -> p.getTitle().toLowerCase().contains(search.toLowerCase()) || 
                                 p.getAuthor().getFullName().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (!"all".equalsIgnoreCase(status)) {
            papers = papers.stream()
                    .filter(p -> p.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        model.addAttribute("papers", papers);
        model.addAttribute("searchQuery", search);
        model.addAttribute("currentStatus", status);

        return "admin/papers"; // Maps to templates/admin/papers.html
    }
}