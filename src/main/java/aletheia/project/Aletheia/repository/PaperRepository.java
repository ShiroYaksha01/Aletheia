package aletheia.project.Aletheia.repository;

import aletheia.project.Aletheia.entity.PaperEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaperRepository extends JpaRepository<PaperEntity, Long> {
    List<PaperEntity> findByAuthorId(Long authorID);
}
