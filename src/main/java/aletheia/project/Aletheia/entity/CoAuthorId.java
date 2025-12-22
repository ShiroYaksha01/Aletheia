package aletheia.project.Aletheia.entity;

import java.io.Serializable;
import java.util.Objects;

public class CoAuthorId implements Serializable {

    private Long paperId;
    private Long authorId;

    public CoAuthorId() {}

    public CoAuthorId(Long paperId, Long authorId) {
        this.paperId = paperId;
        this.authorId = authorId;
    }

    // Getters and Setters
    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    // equals and hashCode are required for composite keys
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoAuthorId that = (CoAuthorId) o;
        return Objects.equals(paperId, that.paperId) && Objects.equals(authorId, that.authorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paperId, authorId);
    }
}