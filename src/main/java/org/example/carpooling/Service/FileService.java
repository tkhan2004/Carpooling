package org.example.carpooling.Service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    boolean saveFile(MultipartFile file);
    Resource loadFile(String fileName);
}
