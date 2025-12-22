package aletheia.project.Aletheia.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class ReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @Column(name = "score", nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "submitted_date", nullable = false)
    private LocalDateTime submittedDate;

    // Many-to-One relationship with PaperEntity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false)
    private PaperEntity paper;

    // Many-to-One relationship with UserEntity (reviewer)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private UserEntity reviewer;

    public ReviewEntity() {}

    // Getters
    public Long getId() {
        return id;
    }

    public BigDecimal getScore() {
        return score;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getSubmittedDate() {
        return submittedDate;
    }

    public PaperEntity getPaper() {
        return paper;
    }

    public UserEntity getReviewer() {
        return reviewer;
    }

    // Setters
    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setSubmittedDate(LocalDateTime submittedDate) {
        this.submittedDate = submittedDate;
    }

    public void setPaper(PaperEntity paper) {
        this.paper = paper;
    }

    public void setReviewer(UserEntity reviewer) {
        this.reviewer = reviewer;
    }
}