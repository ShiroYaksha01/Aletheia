package aletheia.project.Aletheia.controller;

import aletheia.project.Aletheia.dto.PaperRequest;
import aletheia.project.Aletheia.entity.PaperEntity;
import aletheia.project.Aletheia.entity.ReviewEntity;
import aletheia.project.Aletheia.entity.UserEntity;
import aletheia.project.Aletheia.entity.CoAuthorEntity;
import aletheia.project.Aletheia.repository.CoAuthorRepository;
import aletheia.project.Aletheia.repository.PaperRepository;
import aletheia.project.Aletheia.repository.ReviewRepository;
import aletheia.project.Aletheia.repository.UserRepository;
import aletheia.project.Aletheia.service.PaperService;
import jakarta.validation.Valid;

import java.io.IOException;
import java.net.MalformedURLException;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;

@Controller
@RequestMapping("/papers")
public class PaperController {
    private final PaperService paperService;
    private final UserRepository userRepository;
    private final PaperRepository paperRepository;
    private final ReviewRepository reviewRepository;
    private final CoAuthorRepository coAuthorRepository;
    
    @Value("${file.upload-dir:uploads/papers}")
    private String uploadDir;
    
    public PaperController(PaperService paperService, UserRepository userRepository, PaperRepository paperRepository, ReviewRepository reviewRepository, CoAuthorRepository coAuthorRepository) {
        this.paperService = paperService;
        this.userRepository = userRepository;
        this.paperRepository = paperRepository;
        this.reviewRepository = reviewRepository;
        this.coAuthorRepository = coAuthorRepository;
    }

@GetMapping("/submit")
public String showSubmitForm(Model model) {
    model.addAttribute("paperRequest", new PaperRequest());
    model.addAttribute("editMode", false);
    model.addAttribute("pageTitle", "Submit Paper");
    return "papers/submit";
}

    @GetMapping("/my-papers")
    public String myPapers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false, defaultValue = "all") String status,
            Model model) {
        
        // 1. Get current user
        String email = userDetails.getUsername();
        UserEntity currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Fetch authored papers
        List<PaperEntity> authoredPapers = paperRepository.searchMyPapers(currentUser, search, status);
        
        // 3. Fetch co-authored papers
        List<CoAuthorEntity> coAuthoredEntities = coAuthorRepository.findByAuthorIdWithPaper(currentUser.getId());
        List<PaperEntity> coAuthoredPapers = coAuthoredEntities.stream()
                .map(CoAuthorEntity::getPaper)
                .filter(paper -> {
                    if (search != null && !search.trim().isEmpty()) {
                        return paper.getTitle().toLowerCase().contains(search.toLowerCase());
                    }
                    return true;
                })
                .filter(paper -> {
                    if (status != null && !status.equals("all")) {
                        return paper.getStatus().equals(status);
                    }
                    return true;
                })
                .toList();
        
        // 4. Combine and deduplicate papers
        List<PaperEntity> allPapers = new java.util.ArrayList<>(authoredPapers);
        for (PaperEntity coAuthoredPaper : coAuthoredPapers) {
            if (!allPapers.contains(coAuthoredPaper)) {
                allPapers.add(coAuthoredPaper);
            }
        }
        
        // 5. Sort by creation date (most recent first)
        allPapers.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));

        // 6. Add to model
        model.addAttribute("papers", allPapers);
        model.addAttribute("searchQuery", search);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentUserId", currentUser.getId()); // To determine if user is author or co-author

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
        
        // 2. Fetch co-authors
        List<CoAuthorEntity> coAuthorEntities = coAuthorRepository.findByPaperIdWithAuthor(id);
        List<UserEntity> coAuthors = coAuthorEntities.stream()
                .map(CoAuthorEntity::getAuthor)
                .toList();

        // 3. Add to Model
        model.addAttribute("paper", paper);
        model.addAttribute("reviews", reviews);
        model.addAttribute("coAuthors", coAuthors);
        
        // 4. Determine user permissions
        boolean isAuthor = false;
        boolean isCoAuthor = false;
        boolean canViewReviews = false;
        
        if (userDetails != null) {
            String email = userDetails.getUsername();
            UserEntity currentUser = userRepository.findByEmail(email).orElse(null);
            
            if (currentUser != null) {
                // Check if user is the main author
                isAuthor = paper.getAuthor().getId().equals(currentUser.getId());
                
                // Check if user is a co-author
                isCoAuthor = coAuthorRepository.existsByPaperIdAndAuthorId(id, currentUser.getId());
                
                // Can view reviews if user is author, co-author, or admin, and paper status is final
                canViewReviews = (isAuthor || isCoAuthor || currentUser.getRole().equals("ADMIN")) && 
                               (paper.getStatus().equals("ACCEPTED") || paper.getStatus().equals("REJECTED"));
            }
        }
        
        model.addAttribute("isAuthor", isAuthor);
        model.addAttribute("isCoAuthor", isCoAuthor);
        model.addAttribute("canViewReviews", canViewReviews);
        model.addAttribute("pageTitle", "Paper Details");
        model.addAttribute("pageSubtitle", "View detailed information about the paper");

        return "papers/detail";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable("filename") String filename) {
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
        System.out.println("Create paper debug");
        // 1. Check if user is logged in
        if (userDetails == null) {
            System.out.println("Error: User not logged in");
            redirectAttributes.addFlashAttribute("error", "You must be logged in to create a paper.");
            return "redirect:/login";
        }
        System.out.println("User: " + userDetails.getUsername());

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

        // Print form data
        System.out.println("Title: " + paperRequest.getTitle());
        System.out.println("Abstract: " + (paperRequest.getAbstractText() != null ? 
            paperRequest.getAbstractText().substring(0, Math.min(50, paperRequest.getAbstractText().length())) + "..." : "null"));
        System.out.println("Research Area: " + paperRequest.getResearchArea());
        System.out.println("Custom Research Area: " + customResearchArea);


        if (bindingResult.hasErrors()) {
            System.out.println("VALIDATION ERRORS:");
            bindingResult.getAllErrors().forEach(error -> 
                System.out.println("  - " + error.getDefaultMessage())
            );
            model.addAttribute("editMode", false);
            model.addAttribute("paperRequest", paperRequest);
            return "papers/submit";
        }

        try {
            // 3. Fetch User
            String email = userDetails.getUsername();
            System.out.println("Fetching user with email: " + email);
            UserEntity author = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

            // [LOGIC ADDED] Handle Research Area (Dropdown vs Custom Input)
            String finalResearchArea = paperRequest.getResearchArea();
            if ("OTHER".equalsIgnoreCase(finalResearchArea) && customResearchArea != null && !customResearchArea.isBlank()) {
                finalResearchArea = customResearchArea.trim();
            }


            // 4. Call Service with all data
            // NOTE: You must update your PaperService.createPaper signature to accept these new fields!
            PaperEntity createdPaper = paperService.createPaper(
                paperRequest.getTitle(),
                paperRequest.getAbstractText(),
                finalResearchArea, // Passed the processed area
                file,
                author
            );
            
            // 5. Save co-authors if provided
            if (paperRequest.getCoAuthorEmails() != null && !paperRequest.getCoAuthorEmails().trim().isEmpty()) {
                saveCoAuthors(createdPaper.getId(), paperRequest.getCoAuthorEmails());
            }

            redirectAttributes.addFlashAttribute("success", "Paper created successfully!");
            return "redirect:/dashboard";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to upload paper: " + e.getMessage());
            // Important: Add the paperRequest back so the form doesn't go blank on error
            model.addAttribute("paperRequest", paperRequest); 
            model.addAttribute("editMode", false);
            return "papers/submit";
        }
    }

    @GetMapping("/{id}/edit")
    public String editPaper(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        PaperEntity paper = paperService.findById(id)
                .orElseThrow(() -> new RuntimeException("Paper not found"));

        UserEntity user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!paper.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        PaperRequest paperRequest = new PaperRequest();
        paperRequest.setTitle(paper.getTitle());
        paperRequest.setAbstractText(paper.getAbstractText());
        paperRequest.setResearchArea(paper.getResearchArea());

        // Set the file name for display in edit mode
        paperRequest.setFileName(paper.getFileName()); // assuming your PaperEntity has getFileName()
        
        // Get existing co-authors and populate the field
        List<CoAuthorEntity> coAuthorEntities = coAuthorRepository.findByPaperIdWithAuthor(id);
        if (!coAuthorEntities.isEmpty()) {
            String coAuthorEmails = coAuthorEntities.stream()
                    .map(ca -> ca.getAuthor().getEmail())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            paperRequest.setCoAuthorEmails(coAuthorEmails);
        }

        model.addAttribute("paperRequest", paperRequest);
        model.addAttribute("paperId", paper.getId());
        model.addAttribute("editMode", true);
        model.addAttribute("pageTitle", "Edit Paper");

        return "papers/submit";
    }

    @PostMapping("/{id}/edit")
    public String updatePaper(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute PaperRequest paperRequest,
            BindingResult bindingResult,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "customResearchArea", required = false) String customResearchArea,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model
    ) throws Throwable {
        PaperEntity paper = paperService.findById(id)
                .orElseThrow(() -> new RuntimeException("Paper not found"));

        UserEntity currentUser = userRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!paper.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        // Validate new file if uploaded
        if (file != null && !file.isEmpty()) {
            if (!"application/pdf".equals(file.getContentType())) {
                bindingResult.reject("error.file", "Only PDF files allowed");
            }
            if (file.getSize() > 10_000_000) {
                bindingResult.reject("error.file", "File must be ≤ 10MB");
            }
        }

        if (bindingResult.hasErrors()) {
            // Repopulate form with current file info
            paperRequest.setFileName(paper.getFileName());
            model.addAttribute("paperRequest", paperRequest);
            model.addAttribute("paperId", id);
            model.addAttribute("editMode", true);
            return "papers/submit";
        }

        String finalResearchArea = paperRequest.getResearchArea();
        if ("OTHER".equalsIgnoreCase(finalResearchArea) &&
            customResearchArea != null &&
            !customResearchArea.isBlank()) {
            finalResearchArea = customResearchArea.trim();
        }

        paperService.updatePaper(
                paper,
                paperRequest.getTitle(),
                paperRequest.getAbstractText(),
                finalResearchArea,
                file
        );
        
        // Update co-authors
        updateCoAuthors(id, paperRequest.getCoAuthorEmails());

        redirectAttributes.addFlashAttribute("success", "Paper updated successfully!");
        return "redirect:/papers/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deletePaper(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ){ try {
            PaperEntity paper = paperService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Paper not found"));

            UserEntity currentUser = userRepository
                    .findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!paper.getAuthor().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Unauthorized access");
            }

            paperService.deletePaper(id, currentUser.getId());
            //message of Deleted data and file 
            System.out.println("Paper deleted successfully: ID " + id);
            System.out.println("Associated file deleted from storage if it existed.");
            redirectAttributes.addFlashAttribute("success", "Paper deleted successfully!");
            return "redirect:/dashboard";

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to delete paper: " + e.getMessage());
            return "redirect:/papers/" + id;
        }
    }

     @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewPaper(@PathVariable Long id) {
        PaperEntity paper = paperService.findById(id)
                .orElseThrow(() -> new RuntimeException("Paper not found"));
        
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

        try {
            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/pdf";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDisposition(ContentDisposition.inline()
                    .filename(paper.getFileName()) // Use original filename for display
                    .build());

            return ResponseEntity.ok().headers(headers).body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Helper method to save co-authors for a paper
     */
    private void saveCoAuthors(Long paperId, String coAuthorEmails) {
        if (coAuthorEmails == null || coAuthorEmails.trim().isEmpty()) {
            return;
        }
        
        // Parse comma-separated emails
        String[] emails = coAuthorEmails.split(",");
        
        for (String email : emails) {
            email = email.trim();
            if (!email.isEmpty()) {
                // Find user by email
                userRepository.findByEmail(email).ifPresent(coAuthor -> {
                    // Check if co-author relationship doesn't already exist
                    if (!coAuthorRepository.existsByPaperIdAndAuthorId(paperId, coAuthor.getId())) {
                        CoAuthorEntity coAuthorEntity = new CoAuthorEntity(paperId, coAuthor.getId());
                        coAuthorRepository.save(coAuthorEntity);
                    }
                });
            }
        }
    }
    
    /**
     * Helper method to update co-authors for a paper (removes old ones and adds new ones)
     */
    private void updateCoAuthors(Long paperId, String coAuthorEmails) {
        // Remove existing co-authors
        coAuthorRepository.deleteByPaperId(paperId);
        
        // Add new co-authors
        saveCoAuthors(paperId, coAuthorEmails);
    }

}