package aletheia.project.Aletheia.controller;

import aletheia.project.Aletheia.dto.PaperRequest;
import aletheia.project.Aletheia.entity.UserEntity;
import aletheia.project.Aletheia.repository.UserRepository; // [Import Added]
import aletheia.project.Aletheia.service.PaperService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/papers")
public class PaperController {
    private final PaperService paperService;
    private final UserRepository userRepository; // [Field Added]

    // [Constructor Updated] Inject UserRepository
    public PaperController(PaperService paperService, UserRepository userRepository) {
        this.paperService = paperService;
        this.userRepository = userRepository;
    }

    @PostMapping("/create")
    public String createPaper(
        @Valid @ModelAttribute PaperRequest paperRequest,
        BindingResult bindingResult,
        @RequestParam(value = "file", required = false) MultipartFile file,
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
                // NOTE: We cannot use rejectValue("file"...) if 'file' is not in PaperRequest DTO.
                // Using global error instead to prevent crashes.
                bindingResult.reject("error.file", "Only PDF files are allowed.");
            }
            if (file.getSize() > 10_000_000) {
                bindingResult.reject("error.file", "File size must not exceed 10MB");
            }
        }

        if (bindingResult.hasErrors()) {
            return "pages/createpaper";
        }

        try {
            // [FIX STARTS HERE] ------------------------------------------------
            // Fetch the ACTUAL user entity from the database using the email
            String email = userDetails.getUsername();
            UserEntity author = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
            // [FIX ENDS HERE] --------------------------------------------------

            paperService.createPaper(
                paperRequest.getTitle(),
                paperRequest.getAbstractText(),
                file,
                author // Pass the real, database-managed author
            );

            redirectAttributes.addFlashAttribute("success", "Paper created successfully!");
            return "redirect:/dashboard";
            
        } catch (Exception e) {
            e.printStackTrace(); // Print error to console for debugging
            model.addAttribute("error", "Failed to upload paper: " + e.getMessage());
            return "pages/createpaper";
        }
    }
}