package aletheia.project.Aletheia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import aletheia.project.Aletheia.entity.CoAuthorEntity;
import aletheia.project.Aletheia.entity.CoAuthorId;
import aletheia.project.Aletheia.entity.PaperEntity;
import aletheia.project.Aletheia.entity.UserEntity;

import java.util.List;

@Repository
public interface CoAuthorRepository extends JpaRepository<CoAuthorEntity, CoAuthorId> {
    
    // Find co-authors by paper ID
    List<CoAuthorEntity> findByPaperId(Long paperId);
    
    // Find papers co-authored by user ID
    List<CoAuthorEntity> findByAuthorId(Long authorId);
    
    // Find co-authors by paper
    List<CoAuthorEntity> findByPaper(PaperEntity paper);
    
    // Find papers co-authored by user
    List<CoAuthorEntity> findByAuthor(UserEntity author);
    
    // Check if user is co-author of specific paper
    boolean existsByPaperIdAndAuthorId(Long paperId, Long authorId);
    
    // Get co-authors with user details for a paper
    @Query("SELECT ca FROM CoAuthorEntity ca JOIN FETCH ca.author WHERE ca.paperId = :paperId")
    List<CoAuthorEntity> findByPaperIdWithAuthor(@Param("paperId") Long paperId);
    
    // Get papers with paper details for a co-author
    @Query("SELECT ca FROM CoAuthorEntity ca JOIN FETCH ca.paper WHERE ca.authorId = :authorId")
    List<CoAuthorEntity> findByAuthorIdWithPaper(@Param("authorId") Long authorId);
    
    // Delete all co-authors for a paper
    void deleteByPaperId(Long paperId);
    
    // Delete specific co-author from paper
    void deleteByPaperIdAndAuthorId(Long paperId, Long authorId);
}