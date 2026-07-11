package com.neoulteo.global.web;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {
    private final Path communityUploadPath;

    public UploadResourceConfig(@Value("${community.upload-dir:uploads/community}") String uploadDir) {
        this.communityUploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/community/**")
                .addResourceLocations(communityUploadPath.toUri().toString());
    }
}
