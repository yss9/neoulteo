package com.neoulteo.domain.post.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.neoulteo.global.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts/images")
public class PostImageController {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path uploadPath;

    public PostImageController(@Value("${community.upload-dir:uploads/community}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails principal) throws IOException {
        if (principal == null || principal.getUser() == null || principal.getUser().getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Login is required."));
        }
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Image file is required."));
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Only image files are allowed."));
        }

        String extension = extensionOf(image.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Unsupported image extension."));
        }

        Files.createDirectories(uploadPath);
        String fileName = UUID.randomUUID() + "." + extension;
        Path target = uploadPath.resolve(fileName).normalize();
        if (!target.startsWith(uploadPath)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid file path."));
        }

        image.transferTo(target);
        String imageUrl = "/uploads/community/" + fileName;
        return ResponseEntity.ok(Map.of(
                "success", true,
                "imageUrl", imageUrl,
                "storagePath", target.toString()));
    }

    private String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
