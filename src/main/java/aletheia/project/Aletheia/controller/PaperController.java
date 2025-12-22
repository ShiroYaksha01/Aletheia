package aletheia.project.Aletheia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import aletheia.project.Aletheia.entity.PaperEntity;
import aletheia.project.Aletheia.repository.PaperRepository;
import aletheia.project.Aletheia.repository.UserRepository;

@Controller
@RequestMapping("/papers")
public class PaperController {

    @Autowired
    private PaperRepository paperRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String listPapers(Model model) {
        // TODO: fetch all papers and add to model
        return "papers/list";
    }

    @GetMapping("/submit")
    public String showSubmitForm(Model model) {
        // TODO: add empty PaperEntity to model
        return "papers/submit";
    }

    @PostMapping("/submit")
    public String submitPaper(@ModelAttribute PaperEntity paperEntity, Model model) {
        // TODO: save paper with current user as author
        return "redirect:/papers";
    }

    @GetMapping("/{id}")
    public String viewPaper(@PathVariable Long id, Model model) {
        // TODO: find paper by id and add to model
        return "papers/view";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        // TODO: find paper and check if user owns it
        return "papers/edit";
    }

    @PostMapping("/{id}/edit")
    public String updatePaper(@PathVariable Long id, @ModelAttribute PaperEntity paperEntity, Model model) {
        // TODO: update paper if user owns it
        return "redirect:/papers/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deletePaper(@PathVariable Long id) {
        // TODO: delete paper if user owns it
        return "redirect:/papers";
    }

    @GetMapping("/my-papers")
    public String myPapers(Model model) {
        // TODO: get current user's papers
        return "papers/my-papers";
    }
}