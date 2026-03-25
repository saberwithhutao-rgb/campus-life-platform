package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.dto.CommentCreateDTO;
import com.campus.forum.entity.Comment;
import com.campus.forum.service.CommentService;
import com.campus.forum.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping({ "/api/comments" })
public class CommentController {

  private final CommentService commentService;
  private final JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(Example.class);

  public CommentController(CommentService commentService, JwtUtil jwtUtil) {
    this.commentService = commentService;
    this.jwtUtil = jwtUtil;
  }

  /**
   * 发表评论
   */
  @PostMapping
  public Result<Comment> createComment(
      @RequestBody CommentCreateDTO commentCreateDTO,
      @RequestHeader(value = "Authorization") String authorization) {
    try {
      // 从token解析用户ID
      Integer userId = getUserIdFromToken(authorization);
      
      // 参数校验
      if (commentCreateDTO.getPostId() == null) {
        return Result.fail(400, "帖子ID不能为空");
      }
      if (commentCreateDTO.getContent() == null || commentCreateDTO.getContent().isEmpty()) {
        return Result.fail(400, "评论内容不能为空");
      }
      if (commentCreateDTO.getContent().length() > 500) {
        return Result.fail(400, "评论内容长度不能超过500个字符");
      }

      Comment comment = new Comment();
      comment.setPostId(commentCreateDTO.getPostId());
      comment.setUserId(userId);
      comment.setContent(commentCreateDTO.getContent());
      comment.setCreateTime(commentCreateDTO.getCreateTime());
      // 明确设置auditStatus为0，确保等待百度审核结果
      comment.setAuditStatus(0);

      Comment createdComment = commentService.createComment(comment, commentCreateDTO.getImageUrls());
      Result<Comment> result = Result.success(createdComment);
      result.setMsg("评论成功");
      return result;
    } catch (IllegalArgumentException e) {
      return Result.fail(401, e.getMessage());
    } catch (Exception e) {
        logger.error("操作失败，原因: {}", e.getMessage(), e);
      return Result.fail(500, "评论失败：" + e.getMessage());
    }
  }

  /**
   * 根据帖子ID查询评论
   */
  @GetMapping
  public Result<List<Comment>> getCommentsByPostId(@RequestParam Integer postId) {
    try {
      if (postId == null) {
        return Result.fail(400, "帖子ID不能为空");
      }
      List<Comment> comments = commentService.getCommentsByPostId(postId);
      return Result.success(comments);
    } catch (Exception e) {
        logger.error("操作失败，原因: {}", e.getMessage(), e);
      return Result.fail(500, "查询失败：" + e.getMessage());
    }
  }

  /**
   * 删除评论（只能本人删除）
   */
  @DeleteMapping("/{id}")
  public Result<?> deleteComment(
      @PathVariable Integer id,
      @RequestHeader(value = "Authorization") String authorization) {
    try {
      // 从token解析用户ID
      Integer userId = getUserIdFromToken(authorization);
      
      if (id == null) {
        return Result.fail(400, "评论ID不能为空");
      }
      commentService.deleteComment(id, userId);
      return Result.success(null);
    } catch (IllegalArgumentException e) {
      return Result.fail(401, e.getMessage());
    } catch (Exception e) {
        logger.error("操作失败，原因: {}", e.getMessage(), e);
      return Result.fail(500, "删除失败：" + e.getMessage());
    }
  }

  private Integer getUserIdFromToken(String authorization) {
    return jwtUtil.getUserIdFromToken(jwtUtil.extractToken(authorization));
  }
}