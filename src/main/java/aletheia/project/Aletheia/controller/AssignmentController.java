package aletheia.project.Aletheia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import aletheia.project.Aletheia.entity.AssignmentEntity;
import aletheia.project.Aletheia.repository.AssignmentRepository;
import aletheia.project.Aletheia.repository.PaperRepository;
import aletheia.project.Aletheia.repository.UserRepository;

@Controller
@RequestMapping("/assignments")
public class AssignmentController {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private PaperRepository paperRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String listAssignments(Model model) {
        // TODO: get all assignments for admin/editor
        return "assignments/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        // TODO: load papers and reviewers for assignment
        return "assignments/create";
    }

    @PostMapping("/create")
    public String createAssignment(@ModelAttribute AssignmentEntity assignmentEntity, Model model) {
        // TODO: create new assignment
        return "redirect:/assignments";
    }

    @GetMapping("/{id}")
    public String viewAssignment(@PathVariable Long id, Model model) {
        // TODO: find assignment by id
        return "assignments/view";
    }

    @GetMapping("/my-assignments")
    public String myAssignments(Model model) {
        // TODO: get assignments for current reviewer
        return "assignments/my-assignments";
    }

    @PostMapping("/{id}/delete")
    public String deleteAssignment(@PathVariable Long id) {
        // TODO: delete assignment if admin/editor
        return "redirect:/assignments";
    }
}