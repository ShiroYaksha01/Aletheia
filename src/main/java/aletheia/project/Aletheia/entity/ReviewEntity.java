package aletheia.project.Aletheia.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "reviews")
public class ReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    // 1. Matches SQL #3: Score is now NULLABLE
    // Removed 'nullable = false' so it can be null when first assigned
    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    // 2. Matches SQL #6: Renamed 'comment' to 'feedback'
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    // 3. Matches SQL #4: Submitted Date is now NULLABLE
    @Column(name = "submitted_date")
    private LocalDateTime submittedDate;

    // 4. Matches SQL #1: Status (Default 'PENDING')
    @Column(name = "status", nullable = false)
    private String status = "PENDING"; 

    // 5. Matches SQL #2: Deadline
    @Column(name = "deadline")
    private LocalDate deadline;

    // 6. Matches SQL #5: Created At
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false)
    private PaperEntity paper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private UserEntity reviewer;

    // Constructor
    public ReviewEntity() {}

    // --- Lifecycle Hooks ---
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "PENDING";
        }
    }

    // --- Helper Method for UI ---
    // Calculates "5 days left" logic
    public long getDaysLeft() {
        if (deadline == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), deadline);
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public BigDecimal getScore() { return score; }
    public String getFeedback() { return feedback; }
    public LocalDateTime getSubmittedDate() { return submittedDate; }
    public String getStatus() { return status; }
    public LocalDate getDeadline() { return deadline; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public PaperEntity getPaper() { return paper; }
    public UserEntity getReviewer() { return reviewer; }

    public void setScore(BigDecimal score) { this.score = score; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public void setSubmittedDate(LocalDateTime submittedDate) { this.submittedDate = submittedDate; }
    public void setStatus(String status) { this.status = status; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setPaper(PaperEntity paper) { this.paper = paper; }
    public void setReviewer(UserEntity reviewer) { this.reviewer = reviewer; }
}