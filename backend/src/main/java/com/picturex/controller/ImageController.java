package com.picturex.controller;

import com.picturex.entity.Image;
import com.picturex.entity.User;
import com.picturex.service.ImageEditService;
import com.picturex.service.ImageService;
import com.picturex.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "http://localhost:3000")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageEditService imageEditService;

    @Autowired
    private UserService userService;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.thumbnail.path}")
    private String thumbnailPath;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "未授权，请先登录"));
            }
            
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "文件不能为空"));
            }
            
            User user = userService.findByUsername(authentication.getName());
            Image image = imageService.uploadImage(file, user, description);
            // 确保User的images列表不会被序列化
            if (image.getUser() != null) {
                image.getUser().setImages(null);
            }
            return ResponseEntity.ok(image);
        } catch (IllegalArgumentException e) {
            // 参数验证错误，返回400
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "请求参数错误"));
        } catch (IOException e) {
            // IO错误，返回500
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "文件处理失败: " + (e.getMessage() != null ? e.getMessage() : "未知错误")));
        } catch (Exception e) {
            // 其他错误
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "上传失败"));
        }
    }

    @GetMapping
    public ResponseEntity<List<Image>> getUserImages(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        List<Image> images = imageService.getUserImages(user);
        // 确保User的images列表不会被序列化
        images.forEach(img -> {
            if (img.getUser() != null) {
                img.getUser().setImages(null);
            }
        });
        return ResponseEntity.ok(images);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Image> getImage(@PathVariable Long id, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName());
            Image image = imageService.getImageById(id, user);
            // 确保User的images列表不会被序列化
            if (image.getUser() != null) {
                image.getUser().setImages(null);
            }
            return ResponseEntity.ok(image);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/file/{filename}")
    public ResponseEntity<Resource> getImageFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadPath).resolve(filename);
            Resource resource = new FileSystemResource(filePath);
            
            if (resource.exists()) {
                String contentType = Files.probeContentType(filePath);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"))
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/thumbnail/{filename}")
    public ResponseEntity<Resource> getThumbnail(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(thumbnailPath).resolve("thumb_" + filename);
            Resource resource = new FileSystemResource(filePath);
            
            if (resource.exists()) {
                String contentType = Files.probeContentType(filePath);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"))
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Image>> searchImages(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (startDate != null && !startDate.isEmpty()) {
            start = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (endDate != null && !endDate.isEmpty()) {
            end = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        
        List<Image> images = imageService.searchImages(user, keyword, start, end);
        // 确保User的images列表不会被序列化
        images.forEach(img -> {
            if (img.getUser() != null) {
                img.getUser().setImages(null);
            }
        });
        return ResponseEntity.ok(images);
    }

    @PostMapping("/{id}/tags")
    public ResponseEntity<?> addTag(@PathVariable Long id, @RequestBody Map<String, String> request, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName());
            Image image = imageService.addTagToImage(id, request.get("tagName"), user);
            // 确保User的images列表不会被序列化
            if (image.getUser() != null) {
                image.getUser().setImages(null);
            }
            return ResponseEntity.ok(image);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/tags/{tagName}")
    public ResponseEntity<?> removeTag(@PathVariable Long id, @PathVariable String tagName, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName());
            Image image = imageService.removeTagFromImage(id, tagName, user);
            // 确保User的images列表不会被序列化
            if (image.getUser() != null) {
                image.getUser().setImages(null);
            }
            return ResponseEntity.ok(image);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/tags/{tagName}")
    public ResponseEntity<List<Image>> getImagesByTag(@PathVariable String tagName, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        List<Image> images = imageService.getImagesByTag(user, tagName);
        // 确保User的images列表不会被序列化
        images.forEach(img -> {
            if (img.getUser() != null) {
                img.getUser().setImages(null);
            }
        });
        return ResponseEntity.ok(images);
    }

    @PostMapping("/{id}/crop")
    public ResponseEntity<?> cropImage(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request,
            Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName());
            Image image = imageEditService.cropImage(
                    id,
                    request.get("x"),
                    request.get("y"),
                    request.get("width"),
                    request.get("height"),
                    user
            );
            // 确保User的images列表不会被序列化
            if (image.getUser() != null) {
                image.getUser().setImages(null);
            }
            return ResponseEntity.ok(image);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "裁剪失败"));
        }
    }

    @PostMapping("/{id}/adjust-brightness")
    public ResponseEntity<?> adjustBrightness(
            @PathVariable Long id,
            @RequestBody Map<String, Float> request,
            Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName());
            Image image = imageEditService.adjustBrightness(id, request.get("brightness"), user);
            // 确保User的images列表不会被序列化
            if (image.getUser() != null) {
                image.getUser().setImages(null);
            }
            return ResponseEntity.ok(image);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "亮度调整失败"));
        }
    }

    @PostMapping("/{id}/adjust-contrast")
    public ResponseEntity<?> adjustContrast(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName());
            // 处理contrast参数，可能是Integer或Float
            Object contrastObj = request.get("contrast");
            float contrast;
            if (contrastObj instanceof Integer) {
                contrast = ((Integer) contrastObj).floatValue();
            } else if (contrastObj instanceof Float) {
                contrast = (Float) contrastObj;
            } else if (contrastObj instanceof Number) {
                contrast = ((Number) contrastObj).floatValue();
            } else {
                throw new IllegalArgumentException("对比度参数格式错误");
            }
            Image image = imageEditService.adjustContrast(id, contrast, user);
            // 确保User的images列表不会被序列化
            if (image.getUser() != null) {
                image.getUser().setImages(null);
            }
            return ResponseEntity.ok(image);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "对比度调整失败"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable Long id, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName());
            imageService.deleteImage(id, user);
            return ResponseEntity.ok(Map.of("message", "删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

