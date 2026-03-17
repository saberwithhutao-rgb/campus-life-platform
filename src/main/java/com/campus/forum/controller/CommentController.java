package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.entity.Comment;
import com.campus.forum.service.CommentService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.campus.forum.common.annotation.CurrentUser;  // 导入

@RestController
@RequestMapping("/api/forum/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 发表评论 - ✅ 改成 @CurrentUser
     */
    @PostMapping
    public Result<Comment> createComment(@RequestBody Comment comment, @CurrentUser Integer userId) {
        try {
            // 参数校验
            if (comment.getPostId() == null) {
                return Result.fail(400, "帖子ID不能为空");
            }
            if (comment.getContent() == null || comment.getContent().isEmpty()) {
                return Result.fail(400, "评论内容不能为空");
            }
            if (comment.getContent().length() > 500) {
                return Result.fail(400, "评论内容长度不能超过500个字符");
            }

            comment.setUserId(userId);  // ✅ 从 token 设置 userId
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
     * 根据帖子ID查询评论 - 不需要 userId
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
     * 删除评论 - ✅ 改成 @CurrentUser
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteComment(
            @PathVariable Integer id,
            @CurrentUser Integer userId) {  // ✅ 从 token 拿
        try {
            if (id == null) {
                return Result.fail(400, "评论ID不能为空");
            }
            commentService.deleteComment(id, userId);
            return Result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(500, "删除失败：" + e.getMessage());
        }
    }
}