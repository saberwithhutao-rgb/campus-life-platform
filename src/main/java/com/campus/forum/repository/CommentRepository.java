package com.campus.forum.repository;

import com.campus.forum.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
  /**
   * 根据帖子ID查询评论（过滤违规数据）
   */
  List<Comment> findByPostIdAndAuditStatusNot(Integer postId, Integer auditStatus);

  /**
   * 根据帖子ID查询评论，同时加载评论图片（过滤违规数据）
   */
  @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.images WHERE c.postId = ?1 and c.auditStatus != 2")
  List<Comment> findByPostIdWithImages(Integer postId);

  /**
   * 根据ID查询评论，同时加载评论图片（过滤违规数据）
   */
  @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.images WHERE c.id = ?1 and c.auditStatus != 2")
  Optional<Comment> findByIdWithImages(Integer id);
}