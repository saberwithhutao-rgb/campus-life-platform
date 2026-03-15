package com.campus.forum.service.impl;

import com.campus.forum.entity.Post;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.repository.PostRepository;
import com.campus.forum.service.PostService;
import com.campus.forum.util.BloomFilterSensitiveWordFilter;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

  private final PostRepository postRepository;
  private final BloomFilterSensitiveWordFilter sensitiveWordFilter;

  public PostServiceImpl(PostRepository postRepository, BloomFilterSensitiveWordFilter sensitiveWordFilter) {
    this.postRepository = postRepository;
    this.sensitiveWordFilter = sensitiveWordFilter;
  }

  @Override
  public Post createPost(Post post) {
    // 检测标题和内容是否包含敏感词
    if (sensitiveWordFilter.hasSensitivePost(post.getTitle(), post.getContent())) {
      throw new BusinessException(500, "内容包含敏感词汇，无法发布");
    }
    post.setCreateTime(LocalDateTime.now());
    return postRepository.save(post);
  }

  @Override
  public Page<Post> getAllPosts(Pageable pageable) {
    return postRepository.findAll(pageable);
  }

  @Override
  public List<Post> getAllPostsWithoutPagination() {
    return postRepository.findAll();
  }

  @Override
  public Page<Post> getPostsByCategory(Integer categoryId, Pageable pageable) {
    return postRepository.findByCategoryId(categoryId, pageable);
  }

  @Override
  public Post getPostById(Integer id) {
    Post post = postRepository.findByIdWithComments(id);
    if (post == null) {
      throw new BusinessException(404, "帖子不存在");
    }
    return post;
  }

  /**
   * 检查帖子是否存在
   */
  public boolean existsById(Integer id) {
    return postRepository.existsById(id);
  }

  @Override
  public void deletePost(Integer id, Integer userId) {
    Post post = postRepository.findById(id).orElse(null);
    if (post == null) {
      throw new BusinessException(404, "帖子不存在");
    }
    if (!post.getUserId().equals(userId)) {
      throw new BusinessException(403, "权限不足，只能删除自己的帖子");
    }
    postRepository.delete(post);
  }
}