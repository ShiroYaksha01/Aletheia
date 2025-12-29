package aletheia.project.Aletheia.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "papers")
public class PaperEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "abstract_text", columnDefinition = "TEXT") // Good practice for long text
    private String abstractText;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_name")
    private String fileName;
    
    // [ADDED] Status field (Required for Dashboard)
    @Column(nullable = false)
    private String status = "PENDING"; 

    // [ADDED] Research Area (Required for Submission Form)
    @Column(name = "research_area")
    private String researchArea;

    @Column(name = "keywords")
    private String keywords;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity author;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ==========================================
    //           HELPER METHODS (FIXES)
    // ==========================================

    // 1. Fixes "paper.submissionDate" error
    public String getSubmissionDate() {
        if (this.createdAt != null) {
            return this.createdAt.toLocalDate().toString(); // Returns "2023-10-27"
        }
        return "";
    }

    // 2. Fixes "paper.authorNames" error (prevents next crash)
    public String getAuthorNames() {
        if (this.author != null) {
            return this.author.getFirstName() + " " + this.author.getLastName();
        }
        return "Unknown";
    }

    // ==========================================
    //           GETTERS & SETTERS
    // ==========================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAbstractText() { return abstractText; }
    public void setAbstractText(String abstractText) { this.abstractText = abstractText; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public UserEntity getAuthor() { return author; }
    public void setAuthor(UserEntity author) { this.author = author; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // [ADDED] Getters/Setters for new fields
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResearchArea() { return researchArea; }
    public void setResearchArea(String researchArea) { this.researchArea = researchArea; }

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
}