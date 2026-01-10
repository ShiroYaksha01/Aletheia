package aletheia.project.Aletheia.repository;

import aletheia.project.Aletheia.entity.PaperEntity;
import aletheia.project.Aletheia.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaperRepository extends JpaRepository<PaperEntity, Long> {
    List<PaperEntity> findByAuthorId(Long authorID);
    
    PaperEntity findByFilePath(String filePath);

    @Query("SELECT p FROM PaperEntity p WHERE p.author = :author " +
            "AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status = 'all' OR p.status = :status)")
    List<PaperEntity> searchMyPapers(@Param("author") UserEntity author, 
                                    @Param("keyword") String keyword, 
                                    @Param("status") String status);
}
