package aletheia.project.Aletheia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import aletheia.project.Aletheia.entity.PaperEntity;
import aletheia.project.Aletheia.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaperRepository extends JpaRepository<PaperEntity, Long> {
    
    // Find papers by author
    List<PaperEntity> findByAuthor(UserEntity author);
    
    // Find papers by author ID
    List<PaperEntity> findByAuthorId(Long authorId);
    
    // Find papers by title containing (case insensitive search)
    List<PaperEntity> findByTitleContainingIgnoreCase(String title);
    
    // Find papers by author email
    @Query("SELECT p FROM PaperEntity p WHERE p.author.email = :email")
    List<PaperEntity> findByAuthorEmail(@Param("email") String email);
    
    // Find paper by ID with author
    @Query("SELECT p FROM PaperEntity p JOIN FETCH p.author WHERE p.id = :id")
    Optional<PaperEntity> findByIdWithAuthor(@Param("id") Long id);
    
    // Count papers by author
    Long countByAuthorId(Long authorId);
}