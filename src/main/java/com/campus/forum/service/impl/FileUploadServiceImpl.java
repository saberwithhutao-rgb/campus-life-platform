package com.campus.forum.service.impl;

import com.campus.forum.service.FileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {
  private static final String UPLOAD_DIR = "./uploads/";
    @Value("${file.upload.max-file-size:52428800}")  // 默认 50MB
    private long MAX_FILE_SIZE;

    @Value("${file.upload.max-request-size:52428800}")  // 默认 50MB
    private long MAX_REQUEST_SIZE;
  @Override
  public List<String> uploadImages(MultipartFile[] files) throws IOException {
    List<String> imageUrls = new ArrayList<>();

    // 检查文件数组是否为空
    if (files == null || files.length == 0) {
      throw new IOException("No files provided");
    }

    long totalSize = 0;

    // 检查总大小
    for (MultipartFile file : files) {
      if (file != null) {
        totalSize += file.getSize();
        if (file.getSize() > MAX_FILE_SIZE) {
          throw new IOException("File size exceeds 50MB limit");
        }
      }
    }

    if (totalSize > MAX_REQUEST_SIZE) {
      throw new IOException("Total request size exceeds 50MB limit");
    }

    // 创建日期目录
    String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    Path uploadPath = Paths.get(UPLOAD_DIR + datePath);
    try {
      if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
      }
    } catch (IOException e) {
      throw new IOException("Failed to create upload directory: " + e.getMessage());
    }

    // 上传文件
    for (MultipartFile file : files) {
      if (file != null && !file.isEmpty()) {
        try {
          String originalFilename = file.getOriginalFilename();
          if (originalFilename == null || originalFilename.isEmpty()) {
            continue;
          }

          String extension = "";
          int dotIndex = originalFilename.lastIndexOf(".");
          if (dotIndex != -1) {
            extension = originalFilename.substring(dotIndex);
          }

          String filename = UUID.randomUUID().toString() + extension;
          Path filePath = uploadPath.resolve(filename);

          try {
            Files.copy(file.getInputStream(), filePath);
            String imageUrl = "/uploads/" + datePath + "/" + filename;
            imageUrls.add(imageUrl);
          } catch (IOException e) {
            throw new IOException("Failed to save file: " + e.getMessage());
          }
        } catch (Exception e) {
          throw new IOException("Error processing file: " + e.getMessage());
        }
      }
    }

    return imageUrls;
  }
}
