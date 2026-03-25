package com.campus.forum.repository;

import com.campus.forum.entity.CommentImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentImageRepository extends JpaRepository<CommentImage, Integer> {
  List<CommentImage> findByCommentId(Integer commentId);
}