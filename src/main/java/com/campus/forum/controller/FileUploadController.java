package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

  @Autowired
  private FileUploadService fileUploadService;

  @PostMapping("/images")
  public Result uploadImages(@RequestParam("images") MultipartFile[] files) {
    try {
      List<String> imageUrls = fileUploadService.uploadImages(files);
      return Result.success(imageUrls);
    } catch (IOException e) {
      return Result.fail(500, e.getMessage());
    }
  }
}
