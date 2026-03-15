package com.campus.forum.repository;

import com.campus.forum.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Integer> {

  /**
   * 分页查询所有帖子
   */
  Page<Post> findAll(Pageable pageable);

  /**
   * 按分类分页查询帖子
   */
  Page<Post> findByCategoryId(Integer categoryId, Pageable pageable);

  /**
   * 根据ID查询帖子，同时加载评论
   */
  @Query("select p from Post p left join fetch p.comments where p.id = ?1")
  Post findByIdWithComments(Integer id);
}