package aletheia.project.Aletheia.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
public class AssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long id;

    @Column(name = "assigned_date", nullable = false)
    private LocalDateTime assignedDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    // Many-to-One relationship with PaperEntity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false)
    private PaperEntity paper;

    // Many-to-One relationship with UserEntity (reviewer)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private UserEntity reviewer;

    // Many-to-One relationship with UserEntity (assigned by - admin/editor)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private UserEntity assignedBy;

    public AssignmentEntity() {}

    // Getters
    public Long getId() {
        return id;
    }

    public LocalDateTime getAssignedDate() {
        return assignedDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public PaperEntity getPaper() {
        return paper;
    }

    public UserEntity getReviewer() {
        return reviewer;
    }

    public UserEntity getAssignedBy() {
        return assignedBy;
    }

    // Setters
    public void setAssignedDate(LocalDateTime assignedDate) {
        this.assignedDate = assignedDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setPaper(PaperEntity paper) {
        this.paper = paper;
    }

    public void setReviewer(UserEntity reviewer) {
        this.reviewer = reviewer;
    }

    public void setAssignedBy(UserEntity assignedBy) {
        this.assignedBy = assignedBy;
    }
}