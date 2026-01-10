package aletheia.project.Aletheia.controller;

import aletheia.project.Aletheia.entity.PaperEntity;
import aletheia.project.Aletheia.entity.ReviewEntity;
import aletheia.project.Aletheia.entity.UserEntity;
import aletheia.project.Aletheia.repository.ReviewRepository;
import aletheia.project.Aletheia.repository.UserRepository;
import aletheia.project.Aletheia.service.PaperService;
import aletheia.project.Aletheia.repository.PaperRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PaperService paperService;
    private final PaperRepository paperRepository;

    @Value("${file.upload-dir:uploads/papers}")
    private String uploadDir;

    public ReviewController(ReviewRepository reviewRepository, UserRepository userRepository, PaperService paperService, PaperRepository paperRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.paperService = paperService;
        this.paperRepository = paperRepository;
    }
    @GetMapping("/my-reviews")
    public String myReviews(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam(value = "search", required = false) String search,
                            @RequestParam(value = "status", required = false, defaultValue = "all") String status,
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
            model.addAttribute("returnUrl", "/reviews/my-reviews");
            
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
                               @RequestParam(required = false) String returnUrl,
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
            return returnUrl != null ? "redirect:" + returnUrl : "redirect:/reviews/my-reviews";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to submit review.");
            return "redirect:/reviews/submit/" + id;
        }
    }

    // === GET: Edit Completed Review Form ===
    @GetMapping("/edit/{id}")
    public String editReviewForm(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam(required = false) String from,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            // 1. Fetch the review
            ReviewEntity review = reviewRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Review not found"));

            // 2. Security check
            if (!review.getReviewer().getEmail().equals(userDetails.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this review.");
                return "redirect:/reviewer/assigned-papers";
            }

            // 3. Only allow editing if the review is COMPLETED
            if (!"COMPLETED".equalsIgnoreCase(review.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "Only completed reviews can be edited.");
                return "redirect:/reviewer/assigned-papers";
            }

            model.addAttribute("review", review);
            model.addAttribute("paper", review.getPaper());
            model.addAttribute("returnUrl", "/reviews/view/" + id);

            // Back / Cancel logic
            if ("view".equalsIgnoreCase(from)) {
                model.addAttribute("returnUrl", "/reviews/view/" + id);
            } else {
                model.addAttribute("returnUrl", "/reviews/my-reviews");
            }

            return "reviewers/submit"; // reuse the same submit.html form for editing

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading review: " + e.getMessage());
            return "redirect:/reviewer/assigned-papers";
        }
    }

    // === POST: Save Edited Review ===
    @PostMapping("/edit/{id}")
    public String saveEditedReview(@PathVariable Long id,
                                @RequestParam("score") BigDecimal score,
                                @RequestParam("feedback") String feedback,
                                @RequestParam(required = false) String returnUrl,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            ReviewEntity review = reviewRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Review not found"));

            // Security check
            if (!review.getReviewer().getEmail().equals(userDetails.getUsername())) {
                throw new RuntimeException("Unauthorized");
            }

            // Only allow editing completed reviews
            if (!"COMPLETED".equalsIgnoreCase(review.getStatus())) {
                throw new RuntimeException("Only completed reviews can be edited.");
            }

            // Update score and feedback
            review.setScore(score);
            review.setFeedback(feedback);

            reviewRepository.save(review);

            redirectAttributes.addFlashAttribute("success", "Review updated successfully!");
            return returnUrl != null ? "redirect:" + returnUrl : "redirect:/reviews/view/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update review: " + e.getMessage());
            return "redirect:/reviews/edit/" + id;
        }
    }

    // === GET: View Completed Review ===
    @GetMapping("/view/{id}")
    public String viewReview(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            ReviewEntity review = reviewRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Review not found"));

            // Security: Ensure the current user is the reviewer
            if (!review.getReviewer().getEmail().equals(userDetails.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to view this review.");
                return "redirect:/reviewer/assigned-papers";
            }

            // Only allow viewing COMPLETED reviews
            if (!"COMPLETED".equalsIgnoreCase(review.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "You can only view completed reviews.");
                return "redirect:/reviewer/assigned-papers";
            }

            model.addAttribute("review", review);
            model.addAttribute("paper", review.getPaper());
            
            return "reviewers/detail-review";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading review: " + e.getMessage());
            return "redirect:/reviewer/assigned-papers";
        }
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> downloadPaperFile(@PathVariable String filename,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        try {
            UserEntity reviewer = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Find the paper by filePath to get the original filename
            PaperEntity paper = paperRepository.findByFilePath(filename);
            String originalFilename = (paper != null && paper.getFileName() != null) 
                    ? paper.getFileName() 
                    : filename;

            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            Path file = uploadPath.resolve(filename).normalize();
            
            if (!file.startsWith(uploadPath)) {
                return ResponseEntity.notFound().build();
            }
            
            if (!Files.exists(file)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new UrlResource(file.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFilename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{reviewId}/preview")
    public ResponseEntity<Resource> previewPaper(@PathVariable Long reviewId,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // 1. Fetch the review to get the associated paper
            ReviewEntity review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Review not found"));

            // 2. Security check: Ensure current user is the assigned reviewer
            if (!review.getReviewer().getEmail().equals(userDetails.getUsername())) {
                return ResponseEntity.status(403).build();
            }

            PaperEntity paper = review.getPaper();
            
            // Resolve the file path within the upload directory
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            Path filePath = uploadPath.resolve(paper.getFilePath()).normalize();
            
            // Security check: ensure the resolved file is within the upload directory
            if (!filePath.startsWith(uploadPath)) {
                return ResponseEntity.notFound().build();
            }

            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/pdf";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDisposition(ContentDisposition.inline()
                    .filename(paper.getFileName())
                    .build());

            return ResponseEntity.ok().headers(headers).body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

}