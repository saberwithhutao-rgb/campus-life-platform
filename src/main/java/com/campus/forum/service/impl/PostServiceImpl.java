package com.campus.forum.service.impl;

import com.campus.forum.entity.Post;
import com.campus.forum.entity.PostImage;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.repository.PostImageRepository;
import com.campus.forum.repository.PostRepository;
import com.campus.forum.service.AuditService;
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
  private final PostImageRepository postImageRepository;
  private final BloomFilterSensitiveWordFilter sensitiveWordFilter;
  private final AuditService auditService;

  public PostServiceImpl(PostRepository postRepository, PostImageRepository postImageRepository,
      BloomFilterSensitiveWordFilter sensitiveWordFilter, AuditService auditService) {
    this.postRepository = postRepository;
    this.postImageRepository = postImageRepository;
    this.sensitiveWordFilter = sensitiveWordFilter;
    this.auditService = auditService;
  }

  @Override
  public Post createPost(Post post) {
    // 检测标题和内容是否包含敏感词
    if (sensitiveWordFilter.hasSensitivePost(post.getTitle(), post.getContent())) {
      throw new BusinessException(500, "内容包含敏感词汇，无法发布");
    }
    post.setCreateTime(LocalDateTime.now());
    Post savedPost = postRepository.save(post);

    // 异步审核帖子
    auditService.auditPost(savedPost.getId().longValue());

    return savedPost;
  }

  @Override
  public Post createPost(Post post, java.util.List<String> imageUrls) {
    // 检测标题和内容是否包含敏感词（不对图片URL进行过滤）
    if (sensitiveWordFilter.hasSensitivePost(post.getTitle(), post.getContent())) {
      throw new BusinessException(500, "内容包含敏感词汇，无法发布");
    }
    post.setCreateTime(LocalDateTime.now());
    Post savedPost = postRepository.save(post);

    // 保存图片URL
    if (imageUrls != null && !imageUrls.isEmpty()) {
      System.out.println("开始保存帖子图片，帖子ID: " + savedPost.getId());
      System.out.println("图片数量: " + imageUrls.size());
      // 初始化 images 列表
      if (savedPost.getImages() == null) {
        savedPost.setImages(new java.util.ArrayList<>());
      }
      for (String imageUrl : imageUrls) {
        PostImage postImage = new PostImage();
        postImage.setPostId(savedPost.getId());
        postImage.setImageUrl(imageUrl);
        postImageRepository.save(postImage);
        // 将保存的图片添加到帖子的 images 字段中
        savedPost.getImages().add(postImage);
        System.out.println("保存帖子图片成功: " + imageUrl);
      }
      System.out.println("返回帖子，图片数量: " + savedPost.getImages().size());
    }

    // 异步审核帖子
    auditService.auditPost(savedPost.getId().longValue());

    return savedPost;
  }

  @Override
  public Page<Post> getAllPosts(Pageable pageable) {
    return postRepository.findAllWithImages(pageable);
  }

  @Override
  public List<Post> getAllPostsWithoutPagination() {
    return postRepository.findAll();
  }

  @Override
  public Page<Post> getPostsByCategory(Integer categoryId, Pageable pageable) {
    return postRepository.findByCategoryIdWithImages(categoryId, pageable);
  }

  @Override
  public Post getPostById(Integer id) {
    return postRepository.findByIdWithComments(id)
        .orElseThrow(() -> new BusinessException(404, "帖子不存在"));
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