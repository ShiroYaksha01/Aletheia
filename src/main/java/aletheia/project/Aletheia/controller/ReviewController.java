package aletheia.project.Aletheia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import aletheia.project.Aletheia.entity.ReviewEntity;
import aletheia.project.Aletheia.repository.ReviewRepository;
import aletheia.project.Aletheia.repository.PaperRepository;
import aletheia.project.Aletheia.repository.UserRepository;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PaperRepository paperRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String listReviews(Model model) {
        // TODO: get all reviews for admin
        return "reviews/list";
    }

    @GetMapping("/submit")
    public String showReviewForm(Model model) {
        // TODO: show review form for assigned paper
        return "reviews/submit";
    }

    @PostMapping("/submit")
    public String submitReview(@ModelAttribute ReviewEntity reviewEntity, Model model) {
        // TODO: save review for paper
        return "redirect:/reviews/my-reviews";
    }

    @GetMapping("/{id}")
    public String viewReview(@PathVariable Long id, Model model) {
        // TODO: find review by id
        return "reviews/view";
    }

    @GetMapping("/my-reviews")
    public String myReviews(Model model) {
        // TODO: get current user's reviews
        return "reviews/my-reviews";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        // TODO: load review for editing
        return "reviews/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateReview(@PathVariable Long id, @ModelAttribute ReviewEntity reviewEntity, Model model) {
        // TODO: update review if owner
        return "redirect:/reviews/" + id;
    }
}