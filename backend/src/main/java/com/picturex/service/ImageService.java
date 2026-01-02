package com.picturex.service;

// EXIF信息提取 - 暂时注释，等依赖正确配置后再启用
// import com.drewnoakes.metadata.Metadata;
// import com.drewnoakes.metadata.exif.ExifIFD0Directory;
// import com.drewnoakes.metadata.exif.ExifSubIFDDirectory;
// import com.drewnoakes.metadata.exif.GpsDirectory;
// import com.drewnoakes.metadata.ImageMetadataReader;
import com.picturex.entity.Image;
import com.picturex.entity.Tag;
import com.picturex.entity.User;
import com.picturex.repository.ImageRepository;
import com.picturex.repository.TagRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private TagRepository tagRepository;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.thumbnail.path}")
    private String thumbnailPath;

    @Value("${file.upload.thumbnail.width}")
    private int thumbnailWidth;

    @Value("${file.upload.thumbnail.height}")
    private int thumbnailHeight;

    public Image uploadImage(MultipartFile file, User user, String description) throws IOException {
        // 验证文件
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        // 验证文件扩展名
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == originalFilename.length() - 1) {
            throw new IllegalArgumentException("文件必须包含有效的扩展名");
        }
        
        // 创建上传目录
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名
        String extension = originalFilename.substring(lastDotIndex);
        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadDir.resolve(filename);

        // 保存文件
        Files.copy(file.getInputStream(), filePath);

        // 读取图片信息（从保存的文件读取）
        BufferedImage bufferedImage = ImageIO.read(filePath.toFile());
        if (bufferedImage == null) {
            // 删除已保存的无效文件
            Files.deleteIfExists(filePath);
            throw new IllegalArgumentException("文件不是有效的图片格式");
        }
        
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        // 提取EXIF信息
        Image image = new Image();
        image.setFilename(filename);
        image.setOriginalFilename(originalFilename);
        image.setFilePath(filePath.toString());
        image.setWidth(width);
        image.setHeight(height);
        image.setFileSize(file.getSize());
        image.setDescription(description);
        image.setUser(user);

        // 提取EXIF信息（从保存的文件读取）
        try {
            extractExifInfo(filePath.toFile(), image);
        } catch (Exception e) {
            // EXIF提取失败不影响上传
            e.printStackTrace();
        }

        // 生成缩略图
        try {
            String thumbnailFilename = "thumb_" + filename;
            Path thumbnailFilePath = Paths.get(thumbnailPath).resolve(thumbnailFilename);
            Files.createDirectories(thumbnailFilePath.getParent());
            
            Thumbnails.of(filePath.toFile())
                    .size(thumbnailWidth, thumbnailHeight)
                    .keepAspectRatio(true)
                    .toFile(thumbnailFilePath.toFile());
            
            image.setThumbnailPath(thumbnailFilePath.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imageRepository.save(image);
    }

    private void extractExifInfo(File imageFile, Image image) throws IOException {
        // EXIF信息提取功能 - 暂时禁用
        // 等metadata-extractor依赖正确配置后，取消注释以下代码启用EXIF提取
        /*
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
            
            // 提取日期
            ExifSubIFDDirectory exifDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exifDirectory != null && exifDirectory.containsTag(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)) {
                String dateStr = exifDirectory.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (dateStr != null) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
                        image.setExifDate(LocalDateTime.parse(dateStr, formatter));
                    } catch (Exception e) {
                        // 日期解析失败
                    }
                }
            }

            // 提取GPS位置
            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDirectory != null) {
                if (gpsDirectory.containsTag(GpsDirectory.TAG_LATITUDE) && 
                    gpsDirectory.containsTag(GpsDirectory.TAG_LONGITUDE)) {
                    double latitude = gpsDirectory.getDouble(GpsDirectory.TAG_LATITUDE);
                    double longitude = gpsDirectory.getDouble(GpsDirectory.TAG_LONGITUDE);
                    image.setExifLocation(String.format("%.6f, %.6f", latitude, longitude));
                }
            }

            // 提取相机信息
            ExifIFD0Directory ifd0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (ifd0Directory != null) {
                if (ifd0Directory.containsTag(ExifIFD0Directory.TAG_MAKE) || 
                    ifd0Directory.containsTag(ExifIFD0Directory.TAG_MODEL)) {
                    String make = ifd0Directory.getString(ExifIFD0Directory.TAG_MAKE);
                    String model = ifd0Directory.getString(ExifIFD0Directory.TAG_MODEL);
                    if (make != null || model != null) {
                        image.setExifCamera((make != null ? make : "") + " " + (model != null ? model : ""));
                    }
                }
            }
        } catch (Exception e) {
            // EXIF提取失败，不影响主流程
            e.printStackTrace();
        }
        */
    }

    public List<Image> getUserImages(User user) {
        return imageRepository.findByUser(user);
    }

    public List<Image> searchImages(User user, String keyword, LocalDateTime startDate, LocalDateTime endDate) {
        return imageRepository.searchImages(user, keyword, startDate, endDate);
    }

    public Image addTagToImage(Long imageId, String tagName, User user) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("图片不存在"));
        
        if (!image.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权操作此图片");
        }

        Tag tag = tagRepository.findByName(tagName)
                .orElseGet(() -> {
                    Tag newTag = new Tag();
                    newTag.setName(tagName);
                    return tagRepository.save(newTag);
                });

        if (!image.getTags().contains(tag)) {
            image.getTags().add(tag);
        }

        return imageRepository.save(image);
    }

    public Image removeTagFromImage(Long imageId, String tagName, User user) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("图片不存在"));
        
        if (!image.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权操作此图片");
        }

        Tag tag = tagRepository.findByName(tagName)
                .orElseThrow(() -> new RuntimeException("标签不存在"));

        image.getTags().remove(tag);
        return imageRepository.save(image);
    }

    public List<Image> getImagesByTag(User user, String tagName) {
        return imageRepository.findByUserAndTag(user, tagName);
    }

    public void deleteImage(Long imageId, User user) throws IOException {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("图片不存在"));
        
        if (!image.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权删除此图片");
        }

        // 删除文件
        if (image.getFilePath() != null) {
            Files.deleteIfExists(Paths.get(image.getFilePath()));
        }
        if (image.getThumbnailPath() != null) {
            Files.deleteIfExists(Paths.get(image.getThumbnailPath()));
        }

        imageRepository.delete(image);
    }

    public Image getImageById(Long id, User user) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("图片不存在"));
        
        if (!image.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权访问此图片");
        }
        
        return image;
    }
}

