package aletheia.project.Aletheia.controller;

import aletheia.project.Aletheia.dto.PaperRequest;
import aletheia.project.Aletheia.entity.PaperEntity;
import aletheia.project.Aletheia.entity.ReviewEntity;
import aletheia.project.Aletheia.entity.UserEntity;
import aletheia.project.Aletheia.repository.PaperRepository;
import aletheia.project.Aletheia.repository.ReviewRepository;
import aletheia.project.Aletheia.repository.UserRepository;
import aletheia.project.Aletheia.service.PaperService;
import aletheia.project.Aletheia.repository.CoAuthorRepository;
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
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "all") String status,
            Model model) {
        
        // 1. Get current user
        String email = userDetails.getUsername();
        UserEntity currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Fetch papers where user is author
        List<PaperEntity> authoredPapers = paperRepository.searchMyPapers(currentUser, search, status);

        // 3. Fetch papers where user is co-author
        List<aletheia.project.Aletheia.entity.CoAuthorEntity> coAuthorEntities = coAuthorRepository.findByAuthorId(currentUser.getId());
        java.util.Set<Long> coAuthorPaperIds = new java.util.HashSet<>();
        java.util.List<PaperEntity> coAuthoredPapers = new java.util.ArrayList<>();
        for (aletheia.project.Aletheia.entity.CoAuthorEntity ca : coAuthorEntities) {
            PaperEntity paper = ca.getPaper();
            // Filter by search and status
            boolean matches = true;
            if (search != null && !search.isBlank()) {
                String keyword = search.toLowerCase();
                matches = paper.getTitle().toLowerCase().contains(keyword);
            }
            if (matches && status != null && !"all".equals(status)) {
                matches = status.equals(paper.getStatus());
            }
            if (matches) {
                coAuthoredPapers.add(paper);
                coAuthorPaperIds.add(paper.getId());
            }
        }

        // 4. Merge authored and co-authored papers (avoid duplicates)
        java.util.Map<Long, PaperEntity> paperMap = new java.util.LinkedHashMap<>();
        java.util.Set<Long> authorPaperIds = new java.util.HashSet<>();
        for (PaperEntity p : authoredPapers) {
            paperMap.put(p.getId(), p);
            authorPaperIds.add(p.getId());
        }
        for (PaperEntity p : coAuthoredPapers) {
            paperMap.putIfAbsent(p.getId(), p);
        }
        java.util.List<PaperEntity> allPapers = new java.util.ArrayList<>(paperMap.values());

        // 5. Build coAuthorsMap (paperId -> List<CoAuthorEntity>)
        java.util.Map<Long, java.util.List<aletheia.project.Aletheia.entity.CoAuthorEntity>> coAuthorsMap = new java.util.HashMap<>();
        for (PaperEntity p : allPapers) {
            coAuthorsMap.put(p.getId(), coAuthorRepository.findByPaperIdWithAuthor(p.getId()));
        }

        // 6. Add to model
        model.addAttribute("papers", allPapers);
        model.addAttribute("coAuthorsMap", coAuthorsMap);
        model.addAttribute("authorPaperIds", authorPaperIds);
        model.addAttribute("coAuthorPaperIds", coAuthorPaperIds);
        model.addAttribute("searchQuery", search); // To keep input filled
        model.addAttribute("currentStatus", status); // To keep dropdown selected
        model.addAttribute("pageTitle", "My Papers");
        model.addAttribute("pageSubtitle", "Manage all your submitted papers");
        return "papers/my-papers";
    }
    
// Inside PaperController.java

    @GetMapping("/{id}")
    public String viewPaper(@PathVariable("id") Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // 1. Fetch Paper
        PaperEntity paper = paperService.findById(id)
            .orElseThrow(() -> new RuntimeException("Paper not found with id: " + id));

        List<ReviewEntity> reviews = reviewRepository.findByPaperId(id);
        // Fetch co-authors
        List<aletheia.project.Aletheia.entity.CoAuthorEntity> coAuthors = coAuthorRepository.findByPaperId(id);

        model.addAttribute("paper", paper);
        model.addAttribute("reviews", reviews);
        model.addAttribute("coAuthors", coAuthors);

        boolean isAuthor = false;
        boolean isCoAuthor = false;
        boolean canViewReviews = false;
        if (userDetails != null) {
            UserEntity currentUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            if (currentUser != null) {
                isAuthor = currentUser.getId().equals(paper.getAuthor().getId());
                isCoAuthor = coAuthors.stream().anyMatch(ca -> ca.getAuthorId().equals(currentUser.getId()));
                canViewReviews = (isAuthor || isCoAuthor) && (paper.getStatus().equals("ACCEPTED") || paper.getStatus().equals("REJECTED"));
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
        @RequestParam(value = "customResearchArea", required = false) String customResearchArea,
        @RequestParam(value = "coAuthorEmails", required = false) String coAuthorEmails,
        @AuthenticationPrincipal UserDetails userDetails,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to create a paper.");
            return "redirect:/login";
        }
        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType == null || !"application/pdf".equals(contentType)) {
                bindingResult.reject("error.file", "Only PDF files are allowed.");
            }
            if (file.getSize() > 10_000_000) {
                bindingResult.reject("error.file", "File size must not exceed 10MB");
            }
        } else {
            bindingResult.reject("error.file", "Paper file is required.");
        }
        if (bindingResult.hasErrors()) {
            return "papers/submit";
        }
        try {
            String email = userDetails.getUsername();
            UserEntity author = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
            String finalResearchArea = paperRequest.getResearchArea();
            if ("OTHER".equalsIgnoreCase(finalResearchArea) && customResearchArea != null && !customResearchArea.isBlank()) {
                finalResearchArea = customResearchArea.trim();
            }
            PaperEntity paper = paperService.createPaper(
                paperRequest.getTitle(),
                paperRequest.getAbstractText(),
                finalResearchArea,
                file,
                author
            );
            // Handle co-authors
            if (coAuthorEmails != null && !coAuthorEmails.isBlank()) {
                String[] emails = coAuthorEmails.split(",");
                for (String coEmail : emails) {
                    String trimmedEmail = coEmail.trim();
                    if (!trimmedEmail.isEmpty() && !trimmedEmail.equalsIgnoreCase(author.getEmail())) {
                        UserEntity coAuthor = userRepository.findByEmail(trimmedEmail).orElse(null);
                        if (coAuthor != null) {
                            aletheia.project.Aletheia.entity.CoAuthorEntity entity = new aletheia.project.Aletheia.entity.CoAuthorEntity(paper.getId(), coAuthor.getId());
                            entity.setPaper(paper);
                            entity.setAuthor(coAuthor);
                            coAuthorRepository.save(entity);
                        }
                    }
                }
            }
            redirectAttributes.addFlashAttribute("success", "Paper created successfully!");
            return "redirect:/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to upload paper: " + e.getMessage());
            model.addAttribute("paperRequest", paperRequest);
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

        // ✅ Set the file name for display in edit mode
        paperRequest.setFileName(paper.getFileName()); // assuming your PaperEntity has getFileName()

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
            @RequestParam(value = "coAuthorEmails", required = false) String coAuthorEmails,
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

        if (file != null && !file.isEmpty()) {
            if (!"application/pdf".equals(file.getContentType())) {
                bindingResult.reject("error.file", "Only PDF files allowed");
            }
            if (file.getSize() > 10_000_000) {
                bindingResult.reject("error.file", "File must be ≤ 10MB");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("paper", paper);
            return "papers/edit";
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

        // Update co-authors: remove all and re-add
        coAuthorRepository.deleteByPaperId(id);
        if (coAuthorEmails != null && !coAuthorEmails.isBlank()) {
            String[] emails = coAuthorEmails.split(",");
            for (String coEmail : emails) {
                String trimmedEmail = coEmail.trim();
                if (!trimmedEmail.isEmpty() && !trimmedEmail.equalsIgnoreCase(currentUser.getEmail())) {
                    UserEntity coAuthor = userRepository.findByEmail(trimmedEmail).orElse(null);
                    if (coAuthor != null) {
                        aletheia.project.Aletheia.entity.CoAuthorEntity entity = new aletheia.project.Aletheia.entity.CoAuthorEntity(paper.getId(), coAuthor.getId());
                        entity.setPaper(paper);
                        entity.setAuthor(coAuthor);
                        coAuthorRepository.save(entity);
                    }
                }
            }
        }

        redirectAttributes.addFlashAttribute("success", "Paper updated successfully!");
        return "redirect:/papers/" + id;
    }

}