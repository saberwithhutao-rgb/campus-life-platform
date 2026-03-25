package com.campus.forum.service.impl;

import com.campus.forum.entity.Comment;
import com.campus.forum.entity.CommentImage;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.repository.CommentImageRepository;
import com.campus.forum.repository.CommentRepository;
import com.campus.forum.service.AuditService;
import com.campus.forum.service.CommentService;
import com.campus.forum.service.PostService;
import com.campus.forum.util.BloomFilterSensitiveWordFilter;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentImageRepository commentImageRepository;
    private final PostService postService;
    private final BloomFilterSensitiveWordFilter sensitiveWordFilter;
    private final AuditService auditService;

    public CommentServiceImpl(CommentRepository commentRepository, CommentImageRepository commentImageRepository,
            PostService postService,
            BloomFilterSensitiveWordFilter sensitiveWordFilter,
            AuditService auditService) {
        this.commentRepository = commentRepository;
        this.commentImageRepository = commentImageRepository;
        this.postService = postService;
        this.sensitiveWordFilter = sensitiveWordFilter;
        this.auditService = auditService;
    }

    @Override
    public Comment createComment(Comment comment) {
        // 检查帖子是否存在
        if (!postService.existsById(comment.getPostId())) {
            throw new BusinessException(404, "帖子不存在");
        }
        // 检测评论内容是否包含敏感词
        if (sensitiveWordFilter.hasSensitiveComment(comment.getContent())) {
            throw new BusinessException(500, "内容包含敏感词汇，无法发布");
        }
        comment.setCreateTime(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);

        // 异步审核评论
        auditService.auditComment(savedComment.getId().longValue());

        return savedComment;
    }

    @Override
    public Comment createComment(Comment comment, java.util.List<String> imageUrls) {
        // 检查帖子是否存在
        if (!postService.existsById(comment.getPostId())) {
            throw new BusinessException(404, "帖子不存在");
        }
        // 检测评论内容是否包含敏感词（不对图片URL进行过滤）
        if (sensitiveWordFilter.hasSensitiveComment(comment.getContent())) {
            throw new BusinessException(500, "内容包含敏感词汇，无法发布");
        }
        comment.setCreateTime(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);

        // 保存图片URL
        if (imageUrls != null && !imageUrls.isEmpty()) {
            System.out.println("开始保存图片，评论ID: " + savedComment.getId());
            System.out.println("图片数量: " + imageUrls.size());
            // 初始化 images 列表
            if (savedComment.getImages() == null) {
                savedComment.setImages(new java.util.ArrayList<>());
            }
            for (String imageUrl : imageUrls) {
                CommentImage commentImage = new CommentImage();
                commentImage.setCommentId(savedComment.getId());
                commentImage.setImageUrl(imageUrl);
                commentImageRepository.save(commentImage);
                // 将保存的图片添加到评论的 images 字段中
                savedComment.getImages().add(commentImage);
                System.out.println("保存图片成功: " + imageUrl);
            }
        }

        // 异步审核评论
        auditService.auditComment(savedComment.getId().longValue());

        System.out.println("返回评论，图片数量: " + (savedComment.getImages() != null ? savedComment.getImages().size() : 0));
        return savedComment;
    }

    @Override
    public List<Comment> getCommentsByPostId(Integer postId) {
        // 检查帖子是否存在
        if (!postService.existsById(postId)) {
            throw new BusinessException(404, "帖子不存在");
        }
        return commentRepository.findByPostIdWithImages(postId);
    }

    @Override
    public void deleteComment(Integer id, Integer userId) {
        Comment comment = commentRepository.findById(id).orElse(null);
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(403, "权限不足，只能删除自己的评论");
        }
        commentRepository.delete(comment);
    }
}