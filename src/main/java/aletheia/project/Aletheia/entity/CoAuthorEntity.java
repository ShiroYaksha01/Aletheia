package aletheia.project.Aletheia.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "co_authors")
@IdClass(CoAuthorId.class)
public class CoAuthorEntity {

    @Id
    @Column(name = "paper_id")
    private Long paperId;

    @Id
    @Column(name = "author_id")
    private Long authorId;

    // Many-to-One relationship with PaperEntity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", insertable = false, updatable = false)
    private PaperEntity paper;

    // Many-to-One relationship with UserEntity (co-author)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    private UserEntity author;

    public CoAuthorEntity() {}

    public CoAuthorEntity(Long paperId, Long authorId) {
        this.paperId = paperId;
        this.authorId = authorId;
    }

    // Getters
    public Long getPaperId() {
        return paperId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public PaperEntity getPaper() {
        return paper;
    }

    public UserEntity getAuthor() {
        return author;
    }

    // Setters
    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public void setPaper(PaperEntity paper) {
        this.paper = paper;
    }

    public void setAuthor(UserEntity author) {
        this.author = author;
    }
}