package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.entity.Post;
import com.campus.forum.entity.Comment;
import com.campus.forum.repository.PostRepository;
import com.campus.forum.repository.CommentRepository;
import com.campus.forum.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

/**
 * 审核相关控制器
 */
@Slf4j
@RestController
@RequestMapping({ "/api/audit" })
public class AuditController {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final JwtUtil jwtUtil;

    public AuditController(PostRepository postRepository,
                           CommentRepository commentRepository,
                           JwtUtil jwtUtil) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 获取当前用户的违规通知
     *
     * @param authorization Authorization header（Bearer token）
     * @return 违规通知列表
     */
    @GetMapping("/violations")
    public Result<?> getViolationNotifications(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 1. 从 token 中解析当前登录用户的 userId
            Integer currentUserId = getUserIdFromToken(authorization);
            if (currentUserId == null) {
                return Result.fail(401, "未授权，请先登录");
            }

            // 2. 获取当前用户的违规通知
            List<Map<String, Object>> notifications = getUserViolations(currentUserId);

            return Result.success(notifications);

        } catch (IllegalArgumentException e) {
            // token 解析失败
            return Result.fail(401, "Token无效或已过期");
        } catch (Exception e) {
            log.error("获取违规通知失败", e);
            return Result.success(new ArrayList<>());
        }
    }

    /**
     * 从 Authorization header 中解析 userId
     */
    private Integer getUserIdFromToken(String authorization) {
        if (authorization == null || authorization.isEmpty()) {
            log.warn("Authorization header 为空");
            return null;
        }

        try {
            // 提取 token
            String token = jwtUtil.extractToken(authorization);
            if (token == null || token.isEmpty()) {
                log.warn("Token 为空");
                return null;
            }

            // 解析 userId
            return jwtUtil.getUserIdFromToken(token);

        } catch (Exception e) {
            log.error("解析 token 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取指定用户的违规通知
     */
    private List<Map<String, Object>> getUserViolations(Integer userId) {
        List<Map<String, Object>> notifications = new ArrayList<>();

        // 查询用户的违规帖子
        try {
            List<Post> allPosts = postRepository.findAll();
            for (Post post : allPosts) {
                if (post != null
                        && post.getUserId() != null
                        && Objects.equals(post.getUserId(), userId)
                        && post.getAuditStatus() != null
                        && post.getAuditStatus() == 2) {

                    Map<String, Object> notification = new HashMap<>();
                    notification.put("type", "post");
                    notification.put("id", post.getId());
                    notification.put("title", post.getTitle() != null ? post.getTitle() : "");
                    notification.put("message", "您之前发布的帖子内容包含违规信息已自动下架");
                    notification.put("createTime", post.getCreateTime());
                    notifications.add(notification);
                }
            }
        } catch (Exception e) {
            log.error("查询违规帖子失败", e);
        }

        // 查询用户的违规评论
        try {
            List<Comment> allComments = commentRepository.findAll();
            for (Comment comment : allComments) {
                if (comment != null
                        && comment.getUserId() != null
                        && Objects.equals(comment.getUserId(), userId)
                        && comment.getAuditStatus() != null
                        && comment.getAuditStatus() == 2) {

                    Map<String, Object> notification = new HashMap<>();
                    notification.put("type", "comment");
                    notification.put("id", comment.getId());
                    notification.put("postId", comment.getPostId());
                    notification.put("message", "您之前发布的评论内容包含违规信息已自动下架");
                    notification.put("createTime", comment.getCreateTime());
                    notifications.add(notification);
                }
            }
        } catch (Exception e) {
            log.error("查询违规评论失败", e);
        }

        return notifications;
    }
}