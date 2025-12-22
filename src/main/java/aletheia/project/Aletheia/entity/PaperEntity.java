package aletheia.project.Aletheia.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "papers")
public class PaperEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paper_id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "submission_date", nullable = false)
    private LocalDateTime submissionDate;

    // Many-to-One relationship with UserEntity (main author)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author;

    // One-to-Many relationship with CoAuthorEntity
    @OneToMany(mappedBy = "paper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CoAuthorEntity> coAuthors;

    // One-to-Many relationship with AssignmentEntity
    @OneToMany(mappedBy = "paper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<AssignmentEntity> assignments;

    // One-to-Many relationship with ReviewEntity
    @OneToMany(mappedBy = "paper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ReviewEntity> reviews;

    public PaperEntity() {}

    // Getters
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getFilePath() {
        return filePath;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public UserEntity getAuthor() {
        return author;
    }

    public Set<CoAuthorEntity> getCoAuthors() {
        return coAuthors;
    }

    public Set<AssignmentEntity> getAssignments() {
        return assignments;
    }

    public Set<ReviewEntity> getReviews() {
        return reviews;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }

    public void setAuthor(UserEntity author) {
        this.author = author;
    }

    public void setCoAuthors(Set<CoAuthorEntity> coAuthors) {
        this.coAuthors = coAuthors;
    }

    public void setAssignments(Set<AssignmentEntity> assignments) {
        this.assignments = assignments;
    }

    public void setReviews(Set<ReviewEntity> reviews) {
        this.reviews = reviews;
    }
}