package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.entity.Post;
import com.campus.forum.entity.Comment;
import com.campus.forum.repository.PostRepository;
import com.campus.forum.repository.CommentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

/**
 * 审核相关控制器
 */
@RestController
@RequestMapping({ "/api/audit" })
public class AuditController {

  private final PostRepository postRepository;
  private final CommentRepository commentRepository;

  public AuditController(PostRepository postRepository, CommentRepository commentRepository) {
    this.postRepository = postRepository;
    this.commentRepository = commentRepository;
  }

  /**
   * 获取用户违规通知
   * 
   * @param userId 用户ID
   * @return 违规通知列表
   */
  @GetMapping("/violations")
  public Result<?> getViolationNotifications(@RequestParam(required = false) Integer userId) {
    try {
      if (userId == null) {
        return Result.fail(400, "用户ID不能为空");
      }

      // 初始化通知列表
      List<Map<String, Object>> notifications = new ArrayList<>();

      try {
        // 查询用户的违规帖子
        List<Post> allPosts = postRepository.findAll();
        List<Post> violatedPosts = new ArrayList<>();
        if (allPosts != null) {
          for (Post post : allPosts) {
            if (post != null
                && post.getUserId() != null
                && Objects.equals(post.getUserId(), userId)
                && post.getAuditStatus() != null
                && post.getAuditStatus() == 2) {
              violatedPosts.add(post);
            }
          }
        }

        // 添加帖子违规通知
        for (Post post : violatedPosts) {
          if (post != null) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "post");
            notification.put("id", post.getId() != null ? post.getId() : null);
            notification.put("title", post.getTitle() != null ? post.getTitle() : "");
            notification.put("message", "您之前发布的帖子内容包含违规信息已自动下架");
            notification.put("createTime", post.getCreateTime() != null ? post.getCreateTime() : null);
            notifications.add(notification);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        // 捕获异常，继续处理评论
      }

      try {
        // 查询用户的违规评论
        List<Comment> allComments = commentRepository.findAll();
        List<Comment> violatedComments = new ArrayList<>();
        if (allComments != null) {
          for (Comment comment : allComments) {
            if (comment != null
                && comment.getUserId() != null
                && Objects.equals(comment.getUserId(), userId)
                && comment.getAuditStatus() != null
                && comment.getAuditStatus() == 2) {
              violatedComments.add(comment);
            }
          }
        }

        // 添加评论违规通知
        for (Comment comment : violatedComments) {
          if (comment != null) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "comment");
            notification.put("id", comment.getId() != null ? comment.getId() : null);
            notification.put("postId", comment.getPostId() != null ? comment.getPostId() : null);
            notification.put("message", "您之前发布的评论内容包含违规信息已自动下架");
            notification.put("createTime", comment.getCreateTime() != null ? comment.getCreateTime() : null);
            notifications.add(notification);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        // 捕获异常，继续执行
      }

      // 始终返回 200 状态码和通知列表
      return Result.success(notifications);
    } catch (Exception e) {
      e.printStackTrace();
      // 捕获所有异常，返回 200 状态码和空数组
      return Result.success(new ArrayList<>());
    }
  }
}
