package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.entity.Post;
import com.campus.forum.service.AuditService;
import com.campus.forum.service.PostService;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final PostService postService;
    @Getter
    private final AuditService auditService;

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    public TestController(PostService postService, AuditService auditService) {
        this.postService = postService;
        this.auditService = auditService;
    }

    @PostMapping("/audit")
    public Result<?> testAudit() {
        // 创建一个测试帖子
        Post post = new Post();
        post.setTitle("测试标题");
        post.setContent("吸毒、贩毒"); // 明确违规内容
        post.setCategoryId(1);
        post.setUserId(1);
        post.setCreateTime(LocalDateTime.now());

        // 保存帖子
        Post savedPost = postService.createPost(post);
        System.out.println("保存后帖子状态: " + savedPost.getAuditStatus());

        // 等待异步审核完成
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            logger.error("操作失败，原因: {}", e.getMessage(), e);
        }

        // 检查审核状态
        Post updatedPost = postService.getPostById(savedPost.getId());
        System.out.println("审核后帖子状态: " + updatedPost.getAuditStatus());

        return Result.success(updatedPost);
    }

}
