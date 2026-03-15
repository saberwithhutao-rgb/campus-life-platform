package com.campus.forum.repository;

import com.campus.forum.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
  /**
   * 根据帖子ID查询评论
   */
  List<Comment> findByPostId(Integer postId);
}