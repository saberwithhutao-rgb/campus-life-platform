package com.campus.forum.repository;

import com.campus.forum.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Integer> {
  List<PostImage> findByPostId(Integer postId);
}