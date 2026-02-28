package com.kh.spring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CommunityWebConfig implements WebMvcConfigurer {
	
    @org.springframework.beans.factory.annotation.Value("${file.upload.path}")
    private String uploadPath;

    //커뮤니티 전용 WebMvcConfigurer
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // use java.nio.file.Paths to get URI (Handles Windows/Mac/Linux separators automatically)
        String path = java.nio.file.Paths.get(uploadPath, "community").toUri().toString();
        
        // Resource locations must end with a slash
        if (!path.endsWith("/")) {
            path += "/";
        }
        
        registry.addResourceHandler("/community/file/**")
                .addResourceLocations(path);
    }
}
