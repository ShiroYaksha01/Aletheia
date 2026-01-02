package aletheia.project.Aletheia.controller;

import aletheia.project.Aletheia.entity.ReviewEntity;
import aletheia.project.Aletheia.entity.UserEntity;
import aletheia.project.Aletheia.repository.ReviewRepository;
import aletheia.project.Aletheia.repository.UserRepository;
import aletheia.project.Aletheia.service.PaperService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PaperService paperService;

    public ReviewController(ReviewRepository reviewRepository, UserRepository userRepository, PaperService paperService) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.paperService = paperService;
    }
    @GetMapping("/my-reviews")
    public String myReviews(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false, defaultValue = "all") String status,
                            Model model) {
        
        // 1. Get Current Reviewer
        UserEntity reviewer = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Fetch Reviews with Paper details (Optimized Query)
        // Using the method from your ReviewRepository to avoid N+1 issues
        List<ReviewEntity> reviews = reviewRepository.findByReviewerIdWithPaper(reviewer.getId());

        // 3. Filter by Status (in Memory)
        if (!"all".equalsIgnoreCase(status)) {
            reviews = reviews.stream()
                    .filter(r -> r.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        // 4. Filter by Search (Paper Title)
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            reviews = reviews.stream()
                    .filter(r -> r.getPaper().getTitle().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        model.addAttribute("reviews", reviews);
        model.addAttribute("searchQuery", search);
        model.addAttribute("currentStatus", status);

        return "reviewers/my-reviews"; // Maps to templates/reviewers/my-reviews.html
    }

    // === GET: Show Review Form ===
    @GetMapping("/submit/{id}")
    public String showReviewForm(@PathVariable Long id, 
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            // 1. Fetch the Review Assignment
            ReviewEntity review = reviewRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Review assignment not found"));

            // 2. Security Check: Ensure current user is the assigned reviewer
            String currentUserEmail = userDetails.getUsername();
            if (!review.getReviewer().getEmail().equals(currentUserEmail)) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to access this review.");
                return "redirect:/reviewer/assigned-papers";
            }

            model.addAttribute("review", review);
            model.addAttribute("paper", review.getPaper()); // Helper for the view
            
            return "reviewers/submit"; // Maps to templates/reviewers/submit.html

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading review: " + e.getMessage());
            return "redirect:/reviewer/assigned-papers";
        }
    }

    // === POST: Submit Review ===
    @PostMapping("/submit/{id}")
    public String submitReview(@PathVariable Long id,
                               @RequestParam("score") BigDecimal score,
                               @RequestParam("feedback") String feedback,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            ReviewEntity review = reviewRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Review not found"));

            // Security Check
            if (!review.getReviewer().getEmail().equals(userDetails.getUsername())) {
                throw new RuntimeException("Unauthorized");
            }

            // Update Fields
            review.setScore(score);
            review.setFeedback(feedback);
            review.setStatus("COMPLETED");
            review.setSubmittedDate(LocalDateTime.now());

            reviewRepository.save(review);

            // After saving the review, check if the paper's status should be updated
            paperService.updatePaperStatusBasedOnReviews(review.getPaper().getId());


            redirectAttributes.addFlashAttribute("success", "Review submitted successfully!");
            return "redirect:/reviewer/assigned-papers";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to submit review.");
            return "redirect:/reviewers/submit/" + id;
        }
    }
}