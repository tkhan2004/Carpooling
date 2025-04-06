//package org.example.carpooling.Security;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
////    @Override
////    public void addCorsMappings(CorsRegistry registry) {
////        registry.addMapping("/**")
////                .allowedOrigins("http://127.0.0.1:5500") // Cho phép từ domain
////                .allowedMethods("GET", "POST", "PUT", "DELETE")
////                .allowedHeaders("*");
////    }
//@Override
//public void addCorsMappings(CorsRegistry registry) {
//    registry.addMapping("/**") // Allow CORS for all endpoints
//            .allowedOrigins("http://yourfrontenddomain.com") // Replace with your frontend URL
//            .allowedMethods("GET", "POST", "PUT", "DELETE") // Allowed HTTP methods
//            .allowedHeaders("*") // Allow all headers
//            .allowCredentials(true); // If you need to send cookies or credentials
//}
//}
