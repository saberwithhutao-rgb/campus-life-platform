package com.campus.forum.service.impl;

import com.campus.forum.entity.Comment;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.repository.CommentRepository;
import com.campus.forum.service.CommentService;
import com.campus.forum.service.PostService;
import com.campus.forum.util.BloomFilterSensitiveWordFilter;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final BloomFilterSensitiveWordFilter sensitiveWordFilter;

    public CommentServiceImpl(CommentRepository commentRepository, PostService postService,
            BloomFilterSensitiveWordFilter sensitiveWordFilter) {
        this.commentRepository = commentRepository;
        this.postService = postService;
        this.sensitiveWordFilter = sensitiveWordFilter;
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
        return commentRepository.save(comment);
    }

    @Override
    public List<Comment> getCommentsByPostId(Integer postId) {
        // 检查帖子是否存在
        if (!postService.existsById(postId)) {
            throw new BusinessException(404, "帖子不存在");
        }
        return commentRepository.findByPostId(postId);
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