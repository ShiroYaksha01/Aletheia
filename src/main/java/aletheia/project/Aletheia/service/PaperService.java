package aletheia.project.Aletheia.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    public PaperEntity createPaper(String title, String abstractText, MultipartFile file, UserEntity author) throws IOException {
        PaperEntity paper = new PaperEntity();
        paper.setTitle(title);
        paper.setAbstractText(abstractText);
        paper.setAuthor(author);

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

        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

}