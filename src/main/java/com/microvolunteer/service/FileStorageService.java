package com.microvolunteer.service;

import com.microvolunteer.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException ex) {
            throw new RuntimeException("Не вдалося створити директорію для завантаження файлів", ex);
        }
    }

    public String storeFile(MultipartFile file, String subDirectory) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        String fileName = UUID.randomUUID().toString() + fileExtension;

        try {
            if (fileName.contains("..")) {
                throw BusinessException.badRequest("Невірне ім'я файлу: " + fileName);
            }

            Path targetPath = this.uploadPath.resolve(subDirectory);
            Files.createDirectories(targetPath);

            Path targetLocation = targetPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("Файл {} успішно збережено", fileName);

            return subDirectory + "/" + fileName;

        } catch (IOException ex) {
            throw BusinessException.badRequest("Не вдалося зберегти файл " + fileName);
        }
    }

    public void deleteFile(String filePath) {
        try {
            Path fileToDelete = this.uploadPath.resolve(filePath).normalize();
            Files.deleteIfExists(fileToDelete);
            log.info("Файл {} видалено", filePath);
        } catch (IOException ex) {
            log.error("Помилка при видаленні файлу {}", filePath, ex);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}