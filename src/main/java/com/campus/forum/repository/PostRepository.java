package com.campus.forum.repository;

import com.campus.forum.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Integer> {

  /**
   * 分页查询所有帖子（过滤违规数据）
   */
  Page<Post> findByAuditStatusNot(Integer auditStatus, Pageable pageable);

  /**
   * 分页查询所有帖子，同时加载图片（过滤违规数据）
   */
  @Query("select p from Post p left join fetch p.images where p.auditStatus != 2")
  Page<Post> findAllWithImages(Pageable pageable);

  /**
   * 按分类分页查询帖子（过滤违规数据）
   */
  Page<Post> findByCategoryIdAndAuditStatusNot(Integer categoryId, Integer auditStatus, Pageable pageable);

  /**
   * 按分类分页查询帖子，同时加载图片（过滤违规数据）
   */
  @Query("select p from Post p left join fetch p.images where p.categoryId = ?1 and p.auditStatus != 2")
  Page<Post> findByCategoryIdWithImages(Integer categoryId, Pageable pageable);

  /**
   * 根据ID查询帖子，同时加载评论（过滤违规数据）
   */
  @Query("select p from Post p left join fetch p.comments c where p.id = ?1 and (c.auditStatus != 2 or c.id is null)")
  Optional<Post> findByIdWithComments(Integer id);

  /**
   * 根据ID查询帖子，同时加载帖子图片，不加载评论和评论图片
   */
  @Query("select p from Post p left join fetch p.images where p.id = ?1")
  Post findByIdWithPostImages(Integer id);

  /**
   * 根据ID查询帖子，同时加载评论和图片（过滤违规数据）
   */
  @Query("select p from Post p left join fetch p.comments c left join fetch c.images left join fetch p.images where p.id = ?1 and (c.auditStatus != 2 or c.id is null)")
  Post findByIdWithCommentsAndImages(Integer id);
}