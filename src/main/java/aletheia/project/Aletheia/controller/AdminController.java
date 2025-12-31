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

    // === DASHBOARD ===
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // You can add stats here later
        return "dashboard/admin"; // Make sure this template exists
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
}