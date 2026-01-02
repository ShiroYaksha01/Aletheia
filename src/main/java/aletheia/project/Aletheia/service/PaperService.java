package aletheia.project.Aletheia.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List; // [Import Added]
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import aletheia.project.Aletheia.entity.PaperEntity;
import aletheia.project.Aletheia.entity.ReviewEntity;
import aletheia.project.Aletheia.entity.UserEntity;
import aletheia.project.Aletheia.repository.PaperRepository;
import aletheia.project.Aletheia.repository.ReviewRepository;

@Service
public class PaperService {
    private final PaperRepository paperRepository;
    private final ReviewRepository reviewRepository;

    @Value("${file.upload-dir:uploads/papers}")
    private String uploadDir;

    public PaperService(PaperRepository paperRepository, ReviewRepository reviewRepository) {
        this.paperRepository = paperRepository;
        this.reviewRepository = reviewRepository;
    }

    // [Method Updated] Now accepts researchArea and keywords
    public PaperEntity createPaper(String title, 
                                   String abstractText, 
                                   String researchArea, 
                                   MultipartFile file, 
                                   UserEntity author) throws IOException {
        
        PaperEntity paper = new PaperEntity();
        paper.setTitle(title);
        paper.setAbstractText(abstractText);
        paper.setResearchArea(researchArea); // [New Setter]
        paper.setAuthor(author);
        paper.setStatus("PENDING");          // Good practice to set an initial status

        if(file != null && !file.isEmpty()) {
            String filename = saveFile(file);
            paper.setFileName(file.getOriginalFilename());
            paper.setFilePath(filename);
        }
        
        return paperRepository.save(paper);
    }

    private String saveFile (MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Use UUID to prevent filename collisions
        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    public List<PaperEntity> getAllPapers() {
        return paperRepository.findAll();
    }

    public Optional<PaperEntity> findById(Long id) {
        return paperRepository.findById(id);
    }

    public void updatePaperStatusBasedOnReviews(Long paperId) {
        PaperEntity paper = paperRepository.findById(paperId)
            .orElseThrow(() -> new RuntimeException("Paper not found"));

        List<ReviewEntity> reviews = reviewRepository.findByPaperId(paperId);

        // If there are no reviews, do nothing
        if (reviews.isEmpty()) {
            return;
        }

        // Check if all reviews are completed
        boolean allCompleted = reviews.stream().allMatch(r -> "COMPLETED".equalsIgnoreCase(r.getStatus()));

        if (allCompleted) {
            // Calculate average score
            double averageScore = reviews.stream()
                .mapToDouble(r -> r.getScore().doubleValue())
                .average()
                .orElse(0.0);

            // Decide status based on average score
            if (averageScore >= 3.0) { // Example threshold
                paper.setStatus("ACCEPTED");
            } else {
                paper.setStatus("REJECTED");
            }
            paperRepository.save(paper);
        }
        // If not all reviews are completed, the status remains "UNDER_REVIEW"
    }
}