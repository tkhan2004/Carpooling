package org.example.carpooling.Service;

import com.cloudinary.utils.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {
    public Map<String, String> upLoadFile(MultipartFile multipartFile) throws IOException ;

    public void deleteFile(String publicId) throws IOException;
}
