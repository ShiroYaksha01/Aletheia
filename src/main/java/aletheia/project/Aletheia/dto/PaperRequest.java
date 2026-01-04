package aletheia.project.Aletheia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PaperRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    private String title;

    @NotBlank(message = "Abstract is required")
    @Size(min = 50, max = 5000, message = "Abstract must be between 50 and 5000 characters")
    private String abstractText;

    private String status;
    private String researchArea;

    public String getResearchArea() {
        return researchArea;
    }
    public String getStatus() {
        return status;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    public void setResearchArea(String researchArea) {
        this.researchArea = researchArea;
    }

    private String fileName; // for edit mode display

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
