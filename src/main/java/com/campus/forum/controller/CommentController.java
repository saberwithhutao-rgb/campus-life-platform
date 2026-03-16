package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.entity.Comment;
import com.campus.forum.service.CommentService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/forum/comments")
public class CommentController {

  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  /**
   * 发表评论
   */
  @PostMapping
  public Result<Comment> createComment(@RequestBody Comment comment) {
    try {
      // 参数校验
      if (comment.getPostId() == null) {
        return Result.fail(400, "帖子ID不能为空");
      }
      if (comment.getUserId() == null) {
        return Result.fail(400, "用户ID不能为空");
      }
      if (comment.getContent() == null || comment.getContent().isEmpty()) {
        return Result.fail(400, "评论内容不能为空");
      }
      if (comment.getContent().length() > 500) {
        return Result.fail(400, "评论内容长度不能超过500个字符");
      }

      Comment createdComment = commentService.createComment(comment);
      Result<Comment> result = Result.success(createdComment);
      result.setMsg("评论成功");
      return result;
    } catch (Exception e) {
      e.printStackTrace();
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
      e.printStackTrace();
      return Result.fail(500, "查询失败：" + e.getMessage());
    }
  }

  /**
   * 删除评论（只能本人删除）
   */
  @DeleteMapping("/{id}")
  public Result<?> deleteComment(
      @PathVariable Integer id,
      @RequestParam Integer userId) {
    try {
      if (id == null) {
        return Result.fail(400, "评论ID不能为空");
      }
      if (userId == null) {
        return Result.fail(400, "用户ID不能为空");
      }
      commentService.deleteComment(id, userId);
      return Result.success(null);
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "删除失败：" + e.getMessage());
    }
  }
}