package aletheia.project.Aletheia.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List; // [Import Added]
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import aletheia.project.Aletheia.entity.PaperEntity;
import aletheia.project.Aletheia.entity.UserEntity;
import aletheia.project.Aletheia.repository.PaperRepository;

@Service
public class PaperService {
    private final PaperRepository paperRepository;

    @Value("${file.upload-dir:uploads/papers}")
    private String uploadDir;

    public PaperService(PaperRepository paperRepository) {
        this.paperRepository = paperRepository;
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
}