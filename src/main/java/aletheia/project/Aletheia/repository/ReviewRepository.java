package aletheia.project.Aletheia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import aletheia.project.Aletheia.entity.ReviewEntity;
import aletheia.project.Aletheia.entity.PaperEntity;
import aletheia.project.Aletheia.entity.UserEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    
    // Find reviews by reviewer
    List<ReviewEntity> findByReviewer(UserEntity reviewer);
    
    // Find reviews by reviewer ID
    List<ReviewEntity> findByReviewerId(Long reviewerId);
    
    // Find reviews by paper
    List<ReviewEntity> findByPaper(PaperEntity paper);
    
    // Find reviews by paper ID
    List<ReviewEntity> findByPaperId(Long paperId);
    
    // Find reviews by score range
    List<ReviewEntity> findByScoreBetween(BigDecimal minScore, BigDecimal maxScore);
    
    // Find reviews with score greater than or equal
    List<ReviewEntity> findByScoreGreaterThanEqual(BigDecimal minScore);
    
    // Find reviews with score less than or equal
    List<ReviewEntity> findByScoreLessThanEqual(BigDecimal maxScore);
    
    // Find review with full details
    @Query("SELECT r FROM ReviewEntity r JOIN FETCH r.paper JOIN FETCH r.reviewer WHERE r.id = :id")
    Optional<ReviewEntity> findByIdWithDetails(@Param("id") Long id);
    
    // Find reviews for paper with reviewer details
    @Query("SELECT r FROM ReviewEntity r JOIN FETCH r.reviewer WHERE r.paperId = :paperId")
    List<ReviewEntity> findByPaperIdWithReviewer(@Param("paperId") Long paperId);
    
    // Find reviews by reviewer with paper details
    @Query("SELECT r FROM ReviewEntity r JOIN FETCH r.paper WHERE r.reviewerId = :reviewerId")
    List<ReviewEntity> findByReviewerIdWithPaper(@Param("reviewerId") Long reviewerId);
    
    // Check if reviewer has already reviewed the paper
    boolean existsByPaperIdAndReviewerId(Long paperId, Long reviewerId);
    
    // Get average score for a paper
    @Query("SELECT AVG(r.score) FROM ReviewEntity r WHERE r.paperId = :paperId")
    BigDecimal getAverageScoreByPaperId(@Param("paperId") Long paperId);
    
    // Count reviews for paper
    Long countByPaperId(Long paperId);
    
    // Count reviews by reviewer
    Long countByReviewerId(Long reviewerId);
    
    // Find review by paper ID and reviewer ID
    Optional<ReviewEntity> findByPaperIdAndReviewerId(Long paperId, Long reviewerId);
}