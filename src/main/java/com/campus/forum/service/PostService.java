package com.campus.forum.service;

import com.campus.forum.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PostService {
  /**
   * 发布帖子
   */
  Post createPost(Post post);

  /**
   * 发布帖子（带图片）
   */
  Post createPost(Post post, java.util.List<String> imageUrls);

  /**
   * 分页查询所有帖子
   */
  Page<Post> getAllPosts(Pageable pageable);

  /**
   * 查询所有帖子（不分页）
   */
  List<Post> getAllPostsWithoutPagination();

  /**
   * 按分类分页查询帖子
   */
  Page<Post> getPostsByCategory(Integer categoryId, Pageable pageable);

  /**
   * 获取帖子详情（包含评论）
   */
  Post getPostById(Integer id);

  /**
   * 检查帖子是否存在
   */
  boolean existsById(Integer id);

  /**
   * 删除帖子（只能本人删除）
   */
  void deletePost(Integer id, Integer userId);
}