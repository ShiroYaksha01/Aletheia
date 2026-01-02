package aletheia.project.Aletheia.controller;

import aletheia.project.Aletheia.dto.PaperRequest;
import aletheia.project.Aletheia.entity.PaperEntity;
import aletheia.project.Aletheia.entity.ReviewEntity;
import aletheia.project.Aletheia.entity.UserEntity;
import aletheia.project.Aletheia.repository.PaperRepository;
import aletheia.project.Aletheia.repository.ReviewRepository;
import aletheia.project.Aletheia.repository.UserRepository;
import aletheia.project.Aletheia.service.PaperService;
import jakarta.validation.Valid;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpHeaders;

@Controller
@RequestMapping("/papers")
public class PaperController {
    private final PaperService paperService;
    private final UserRepository userRepository;
    private final PaperRepository paperRepository;
    private final ReviewRepository reviewRepository;
    
    @Value("${file.upload-dir:uploads/papers}")
    private String uploadDir;
    
    public PaperController(PaperService paperService, UserRepository userRepository, PaperRepository paperRepository, ReviewRepository reviewRepository) {
        this.paperService = paperService;
        this.userRepository = userRepository;
        this.paperRepository = paperRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/submit") // URL becomes /papers/submit
    public String showSubmitForm(Model model) {
        model.addAttribute("paperRequest", new PaperRequest());
        model.addAttribute("pageTitle", "Submit Paper");
        return "papers/submit";
    }

    @GetMapping("/my-papers")
    public String myPapers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "all") String status,
            Model model) {
        
        // 1. Get current user
        String email = userDetails.getUsername();
        UserEntity currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Fetch data using the Repository Query
        List<PaperEntity> papers = paperRepository.searchMyPapers(currentUser, search, status);

        // 3. Add to model
        model.addAttribute("papers", papers);
        model.addAttribute("searchQuery", search); // To keep input filled
        model.addAttribute("currentStatus", status); // To keep dropdown selected

        model.addAttribute("pageTitle", "My Papers");
        model.addAttribute("pageSubtitle", "Manage all your submitted papers");
        return "papers/my-papers";
    }
    
// Inside PaperController.java

    @GetMapping("/{id}")
    public String viewPaper(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // 1. Fetch Paper
        PaperEntity paper = paperService.findById(id)
            .orElseThrow(() -> new RuntimeException("Paper not found with id: " + id));

        List<ReviewEntity> reviews = reviewRepository.findByPaperId(id);

        // 2. Add to Model
        model.addAttribute("paper", paper);
        model.addAttribute("reviews", reviews);
        
        // 3. Determine if current user is the author (to show/hide specific buttons)
        if (userDetails != null) {
            UserEntity currentUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            boolean isAuthor = currentUser != null && currentUser.getId().equals(paper.getAuthor().getId());
            boolean canViewReviews = isAuthor && (paper.getStatus().equals("ACCEPTED") || paper.getStatus().equals("REJECTED"));
            
            model.addAttribute("isAuthor", isAuthor);
            model.addAttribute("canViewReviews", canViewReviews);
        }

        model.addAttribute("pageTitle", "Paper Details");
        model.addAttribute("pageSubtitle", "View detailed information about the paper");

        return "papers/detail"; // Maps to templates/papers/detail.html
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            // Get the absolute path to the uploads directory
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            Path file = uploadPath.resolve(filename).normalize();
            
            // Security check: ensure the resolved file is within the upload directory
            if (!file.startsWith(uploadPath)) {
                throw new IllegalArgumentException("Invalid file path");
            }
            
            // Check if file exists
            if (!Files.exists(file)) {
                throw new RuntimeException("File not found: " + filename);
            }
            
            Resource resource = new UrlResource(file.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (IllegalArgumentException | IOException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/create")
    public String createPaper(
        @Valid @ModelAttribute PaperRequest paperRequest,
        BindingResult bindingResult,
        @RequestParam(value = "file", required = false) MultipartFile file,
        // [ADDED] Capture the custom input field for "Other" area
        @RequestParam(value = "customResearchArea", required = false) String customResearchArea,
        @AuthenticationPrincipal UserDetails userDetails,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        // 1. Check if user is logged in
        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to create a paper.");
            return "redirect:/login";
        }

        // 2. Validate File
        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType == null || !"application/pdf".equals(contentType)) {
                bindingResult.reject("error.file", "Only PDF files are allowed.");
            }
            if (file.getSize() > 10_000_000) {
                bindingResult.reject("error.file", "File size must not exceed 10MB");
            }
        } else {
             // Optional: If file is mandatory, reject here
             bindingResult.reject("error.file", "Paper file is required.");
        }

        if (bindingResult.hasErrors()) {
            return "papers/submit";
        }

        try {
            // 3. Fetch User
            String email = userDetails.getUsername();
            UserEntity author = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

            // [LOGIC ADDED] Handle Research Area (Dropdown vs Custom Input)
            String finalResearchArea = paperRequest.getResearchArea();
            if ("OTHER".equalsIgnoreCase(finalResearchArea) && customResearchArea != null && !customResearchArea.isBlank()) {
                finalResearchArea = customResearchArea.trim();
            }


            // 4. Call Service with all data
            // NOTE: You must update your PaperService.createPaper signature to accept these new fields!
            paperService.createPaper(
                paperRequest.getTitle(),
                paperRequest.getAbstractText(),
                finalResearchArea, // Passed the processed area
                file,
                author
            );

            redirectAttributes.addFlashAttribute("success", "Paper created successfully!");
            return "redirect:/dashboard";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to upload paper: " + e.getMessage());
            // Important: Add the paperRequest back so the form doesn't go blank on error
            model.addAttribute("paperRequest", paperRequest); 
            return "papers/submit";
        }
    }
}