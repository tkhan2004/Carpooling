package org.example.carpooling.Service.Imp;

import org.example.carpooling.Service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileServiceImp implements FileService {

    @Value("${file.upload.path}")
    private String uploadPath;

    private Path root;

    public void init() {
        root = Paths.get(uploadPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            System.out.println("Could not create upload folder: " + e.getMessage());
        }
    }

    @Override
    public String saveFile(MultipartFile file) {
        init();
        try {
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path target = root.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Saved: " + filename);
            return filename;
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Resource loadFile(String fileName) {
        init();
        try {
            Path file = root.resolve(fileName).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
        } catch (MalformedURLException e) {
            System.out.println("Error loading file: " + e.getMessage());
        }
        return null;
    }

    @Override
    public String generateFileUrl(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "/default-avatar.png"; // hoặc null tùy UI của bạn
        }

        if (filename.startsWith("uploads/")) {
            filename = filename.substring("uploads/".length());
        }

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/file/view/")
                .path(filename)
                .toUriString();
    }
}