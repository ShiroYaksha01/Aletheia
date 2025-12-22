package aletheia.project.Aletheia.controller;

import aletheia.project.Aletheia.dto.PaperRequest;
import aletheia.project.Aletheia.entity.UserEntity;
import aletheia.project.Aletheia.service.PaperService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
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

    public PaperController(PaperService paperService) {
        this.paperService = paperService;
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("paperRequest", new PaperRequest());
        return "papers/create";
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
        if (file != null && !file.isEmpty()) {
            if (!file.getContentType().equals("application/pdf")) {
                bindingResult.rejectValue("file", "error.file", "Only PDF files are allowed.");
            }
            if (file.getSize() > 10_000_000) {
                bindingResult.rejectValue("file", "error.file", "File size must not exceed 10MB");
            }
        }
        
        if (bindingResult.hasErrors()) {
            return "papers/create";
        }

        try {
            UserEntity author = new UserEntity();
            author.setEmail(userDetails.getUsername());

            paperService.createPaper(
                paperRequest.getTitle(),
                paperRequest.getAbstractText(),
                file,
                author
            );

            redirectAttributes.addFlashAttribute("success", "Paper created successfully!");
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to upload paper: " + e.getMessage());
            return "papers/create";
        }
    }
    
}
