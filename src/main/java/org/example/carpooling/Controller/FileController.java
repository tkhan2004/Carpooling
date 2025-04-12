package org.example.carpooling.Controller;

import org.example.carpooling.Service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        String savedFilename = String.valueOf(fileService.saveFile(file));
        if (savedFilename != null) {
            String url = fileService.generateFileUrl(savedFilename);
            return ResponseEntity.ok(url);
        }
        return ResponseEntity.status(500).body("Upload failed");
    }

    @GetMapping("/view/{fileName:.+}")
    public ResponseEntity<Resource> view(@PathVariable String fileName) {
        Resource resource = fileService.loadFile(fileName);
        if (resource != null) {
            try {
                String contentType = Files.probeContentType(resource.getFile().toPath());
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .body(resource);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(500).build();
            }
        }
        return ResponseEntity.notFound().build();
    }
}