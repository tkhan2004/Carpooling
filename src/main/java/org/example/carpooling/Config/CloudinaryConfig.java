package org.example.carpooling.Config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dxzk5p80d");
        config.put("api_key","144885277356732");
        config.put("api_secret", "UzqKa5b-EjwkqUCtGaNaRBfBFU0");
        return new Cloudinary(config);
    }
}

