package com.campus.forum.service;

import com.campus.forum.entity.Comment;
import java.util.List;

public interface CommentService {
    /**
     * 发表评论
     */
    Comment createComment(Comment comment);

    /**
     * 发表评论（带图片）
     */
    Comment createComment(Comment comment, java.util.List<String> imageUrls);

    /**
     * 根据帖子ID查询评论
     */
    List<Comment> getCommentsByPostId(Integer postId);

    /**
     * 删除评论（只能本人删除）
     */
    void deleteComment(Integer id, Integer userId);
}