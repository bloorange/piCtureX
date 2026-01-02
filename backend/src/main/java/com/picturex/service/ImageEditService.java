package com.picturex.service;

import com.picturex.entity.Image;
import com.picturex.entity.User;
import com.picturex.repository.ImageRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageEditService {

    @Autowired
    private ImageRepository imageRepository;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.thumbnail.path}")
    private String thumbnailPath;

    @Value("${file.upload.thumbnail.width}")
    private int thumbnailWidth;

    @Value("${file.upload.thumbnail.height}")
    private int thumbnailHeight;

    public Image cropImage(Long imageId, int x, int y, int width, int height, User user) throws IOException {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("图片不存在"));
        
        if (!image.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权操作此图片");
        }

        // 使用正确的路径读取原图
        Path originalPath = Paths.get(uploadPath).resolve(image.getFilename());
        if (!Files.exists(originalPath)) {
            throw new IOException("原图文件不存在: " + originalPath);
        }

        BufferedImage originalImage = ImageIO.read(originalPath.toFile());
        if (originalImage == null) {
            throw new IOException("无法读取图片文件");
        }

        // 验证裁剪参数
        if (x < 0 || y < 0 || width <= 0 || height <= 0 || 
            x + width > originalImage.getWidth() || y + height > originalImage.getHeight()) {
            throw new IllegalArgumentException("裁剪参数超出图片范围");
        }

        BufferedImage croppedImage = originalImage.getSubimage(x, y, width, height);

        // 获取原始文件扩展名
        String originalFilename = image.getFilename();
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
            extension = originalFilename.substring(dotIndex + 1).toLowerCase();
        }
        if (extension.isEmpty() || !extension.matches("jpg|jpeg|png|gif|bmp")) {
            extension = "jpg"; // 默认使用jpg
        }

        // 保存裁剪后的图片
        String croppedFilename = UUID.randomUUID().toString() + "." + extension;
        Path croppedPath = Paths.get(uploadPath).resolve(croppedFilename);
        Files.createDirectories(croppedPath.getParent());
        ImageIO.write(croppedImage, extension, croppedPath.toFile());

        // 生成缩略图
        String thumbnailFilename = "thumb_" + croppedFilename;
        Path thumbnailFilePath = Paths.get(thumbnailPath).resolve(thumbnailFilename);
        Files.createDirectories(thumbnailFilePath.getParent());
        Thumbnails.of(croppedPath.toFile())
                .size(thumbnailWidth, thumbnailHeight)
                .keepAspectRatio(true)
                .toFile(thumbnailFilePath.toFile());

        // 创建新图片记录
        Image newImage = new Image();
        newImage.setFilename(croppedFilename);
        newImage.setOriginalFilename("cropped_" + image.getOriginalFilename());
        newImage.setFilePath(croppedPath.toString());
        newImage.setThumbnailPath(thumbnailFilePath.toString());
        newImage.setWidth(width);
        newImage.setHeight(height);
        newImage.setFileSize(Files.size(croppedPath));
        newImage.setUser(user);
        newImage.setDescription("裁剪自: " + image.getOriginalFilename());

        return imageRepository.save(newImage);
    }

    public Image adjustBrightness(Long imageId, float brightness, User user) throws IOException {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("图片不存在"));
        
        if (!image.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权操作此图片");
        }

        // 使用正确的路径读取原图
        Path originalPath = Paths.get(uploadPath).resolve(image.getFilename());
        if (!Files.exists(originalPath)) {
            throw new IOException("原图文件不存在: " + originalPath);
        }

        BufferedImage originalImage = ImageIO.read(originalPath.toFile());
        if (originalImage == null) {
            throw new IOException("无法读取图片文件");
        }

        BufferedImage adjustedImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = adjustedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();

        // 调整亮度
        for (int y = 0; y < adjustedImage.getHeight(); y++) {
            for (int x = 0; x < adjustedImage.getWidth(); x++) {
                Color color = new Color(adjustedImage.getRGB(x, y));
                int r = Math.min(255, Math.max(0, (int)(color.getRed() * brightness)));
                int g = Math.min(255, Math.max(0, (int)(color.getGreen() * brightness)));
                int b = Math.min(255, Math.max(0, (int)(color.getBlue() * brightness)));
                adjustedImage.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }

        // 获取原始文件扩展名
        String originalFilename = image.getFilename();
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
            extension = originalFilename.substring(dotIndex + 1).toLowerCase();
        }
        if (extension.isEmpty() || !extension.matches("jpg|jpeg|png|gif|bmp")) {
            extension = "jpg"; // 默认使用jpg
        }

        // 保存调整后的图片
        String adjustedFilename = UUID.randomUUID().toString() + "." + extension;
        Path adjustedPath = Paths.get(uploadPath).resolve(adjustedFilename);
        Files.createDirectories(adjustedPath.getParent());
        ImageIO.write(adjustedImage, extension, adjustedPath.toFile());

        // 生成缩略图
        String thumbnailFilename = "thumb_" + adjustedFilename;
        Path thumbnailFilePath = Paths.get(thumbnailPath).resolve(thumbnailFilename);
        Files.createDirectories(thumbnailFilePath.getParent());
        Thumbnails.of(adjustedPath.toFile())
                .size(thumbnailWidth, thumbnailHeight)
                .keepAspectRatio(true)
                .toFile(thumbnailFilePath.toFile());

        // 创建新图片记录
        Image newImage = new Image();
        newImage.setFilename(adjustedFilename);
        newImage.setOriginalFilename("brightness_" + image.getOriginalFilename());
        newImage.setFilePath(adjustedPath.toString());
        newImage.setThumbnailPath(thumbnailFilePath.toString());
        newImage.setWidth(adjustedImage.getWidth());
        newImage.setHeight(adjustedImage.getHeight());
        newImage.setFileSize(Files.size(adjustedPath));
        newImage.setUser(user);
        newImage.setDescription("亮度调整自: " + image.getOriginalFilename());

        return imageRepository.save(newImage);
    }

    public Image adjustContrast(Long imageId, float contrast, User user) throws IOException {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("图片不存在"));
        
        if (!image.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权操作此图片");
        }

        // 使用正确的路径读取原图
        Path originalPath = Paths.get(uploadPath).resolve(image.getFilename());
        if (!Files.exists(originalPath)) {
            throw new IOException("原图文件不存在: " + originalPath);
        }

        BufferedImage originalImage = ImageIO.read(originalPath.toFile());
        if (originalImage == null) {
            throw new IOException("无法读取图片文件");
        }

        BufferedImage adjustedImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = adjustedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();

        // 调整对比度（contrast参数范围通常是50-150，需要转换为-255到255的范围）
        // 将50-150映射到-128到128
        float normalizedContrast = (contrast - 100f) / 100f * 128f;
        float factor = (259f * (normalizedContrast + 255f)) / (255f * (259f - normalizedContrast));
        
        for (int y = 0; y < adjustedImage.getHeight(); y++) {
            for (int x = 0; x < adjustedImage.getWidth(); x++) {
                Color color = new Color(adjustedImage.getRGB(x, y));
                int r = Math.min(255, Math.max(0, (int)(factor * (color.getRed() - 128) + 128)));
                int g = Math.min(255, Math.max(0, (int)(factor * (color.getGreen() - 128) + 128)));
                int b = Math.min(255, Math.max(0, (int)(factor * (color.getBlue() - 128) + 128)));
                adjustedImage.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }

        // 获取原始文件扩展名
        String originalFilename = image.getFilename();
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
            extension = originalFilename.substring(dotIndex + 1).toLowerCase();
        }
        if (extension.isEmpty() || !extension.matches("jpg|jpeg|png|gif|bmp")) {
            extension = "jpg"; // 默认使用jpg
        }

        // 保存调整后的图片
        String adjustedFilename = UUID.randomUUID().toString() + "." + extension;
        Path adjustedPath = Paths.get(uploadPath).resolve(adjustedFilename);
        Files.createDirectories(adjustedPath.getParent());
        ImageIO.write(adjustedImage, extension, adjustedPath.toFile());

        // 生成缩略图
        String thumbnailFilename = "thumb_" + adjustedFilename;
        Path thumbnailFilePath = Paths.get(thumbnailPath).resolve(thumbnailFilename);
        Files.createDirectories(thumbnailFilePath.getParent());
        Thumbnails.of(adjustedPath.toFile())
                .size(thumbnailWidth, thumbnailHeight)
                .keepAspectRatio(true)
                .toFile(thumbnailFilePath.toFile());

        // 创建新图片记录
        Image newImage = new Image();
        newImage.setFilename(adjustedFilename);
        newImage.setOriginalFilename("contrast_" + image.getOriginalFilename());
        newImage.setFilePath(adjustedPath.toString());
        newImage.setThumbnailPath(thumbnailFilePath.toString());
        newImage.setWidth(adjustedImage.getWidth());
        newImage.setHeight(adjustedImage.getHeight());
        newImage.setFileSize(Files.size(adjustedPath));
        newImage.setUser(user);
        newImage.setDescription("对比度调整自: " + image.getOriginalFilename());

        return imageRepository.save(newImage);
    }
}

