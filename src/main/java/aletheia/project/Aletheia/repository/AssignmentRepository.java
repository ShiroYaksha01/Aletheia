package aletheia.project.Aletheia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import aletheia.project.Aletheia.entity.AssignmentEntity;
import aletheia.project.Aletheia.entity.PaperEntity;
import aletheia.project.Aletheia.entity.UserEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<AssignmentEntity, Long> {
    
    // Find assignments by reviewer
    List<AssignmentEntity> findByReviewer(UserEntity reviewer);
    
    // Find assignments by reviewer ID
    List<AssignmentEntity> findByReviewerId(Long reviewerId);
    
    // Find assignments by paper
    List<AssignmentEntity> findByPaper(PaperEntity paper);
    
    // Find assignments by paper ID
    List<AssignmentEntity> findByPaperId(Long paperId);
    
    // Find assignments assigned by user
    List<AssignmentEntity> findByAssignedBy(UserEntity assignedBy);
    
    // Find assignments assigned by user ID
    List<AssignmentEntity> findByAssignedById(Long assignedById);
    
    // Find assignments by due date
    List<AssignmentEntity> findByDueDate(LocalDate dueDate);
    
    // Find assignments due before date
    List<AssignmentEntity> findByDueDateBefore(LocalDate date);
    
    // Find assignments due after date
    List<AssignmentEntity> findByDueDateAfter(LocalDate date);
    
    // Find overdue assignments (due date passed)
    @Query("SELECT a FROM AssignmentEntity a WHERE a.dueDate < CURRENT_DATE")
    List<AssignmentEntity> findOverdueAssignments();
    
    // Find assignments with full details
    @Query("SELECT a FROM AssignmentEntity a JOIN FETCH a.paper JOIN FETCH a.reviewer JOIN FETCH a.assignedBy WHERE a.id = :id")
    Optional<AssignmentEntity> findByIdWithDetails(@Param("id") Long id);
    
    // Find reviewer assignments with paper details
    @Query("SELECT a FROM AssignmentEntity a JOIN FETCH a.paper WHERE a.reviewerId = :reviewerId")
    List<AssignmentEntity> findByReviewerIdWithPaper(@Param("reviewerId") Long reviewerId);
    
    // Check if reviewer is already assigned to paper
    boolean existsByPaperIdAndReviewerId(Long paperId, Long reviewerId);
    
    // Count assignments for reviewer
    Long countByReviewerId(Long reviewerId);
    
    // Count assignments for paper
    Long countByPaperId(Long paperId);
}