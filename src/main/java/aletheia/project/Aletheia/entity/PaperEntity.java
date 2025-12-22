package aletheia.project.Aletheia.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;


@Entity
@Table(name = "papers")
public class PaperEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "abstract_text")
    private String abstractText;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_name")
    private String fileName;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity author;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return this.id;
    }
    public String getTitle() {
        return this.title;
    }
    public String getAbstractText() {
        return this.abstractText;
    }
    public String getFilePath() {
        return this.filePath;
    }
    public UserEntity getAuthor() {
        return this.author;
    }
    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
    public String getFileName() {
        return this.fileName;
    }

    // Setters
    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public void setAuthor(UserEntity author) {
        this.author = author;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
