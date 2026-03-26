package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.dto.PostCreateDTO;
import com.campus.forum.entity.Comment;
import com.campus.forum.entity.Post;
import com.campus.forum.repository.PostRepository;
import com.campus.forum.service.CommentService;
import com.campus.forum.service.PostService;
import com.campus.forum.util.JwtUtil;
import com.campus.forum.user.repository.UserRepository;
import com.campus.forum.user.entity.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping({ "/api/posts" })
public class PostController {

  private final PostService postService;
  private final CommentService commentService;
  private final PostRepository postRepository;
  private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

  public PostController(PostService postService, CommentService commentService, PostRepository postRepository, JwtUtil jwtUtil, UserRepository userRepository) {
    this.postService = postService;
    this.commentService = commentService;
    this.postRepository = postRepository;
    this.jwtUtil = jwtUtil;
      this.userRepository = userRepository;
  }

  /**
   * 发布帖子
   */
  @PostMapping
  public Result<Post> createPost(
      @RequestBody PostCreateDTO postCreateDTO,
      @RequestHeader(value = "Authorization") String authorization) {
    try {
      // 从token解析用户ID
      Integer userId = getUserIdFromToken(authorization);
      
      // 参数校验
      if (postCreateDTO.getTitle() == null || postCreateDTO.getTitle().isEmpty()) {
        return Result.fail(400, "标题不能为空");
      }
      if (postCreateDTO.getTitle().length() > 100) {
        return Result.fail(400, "标题长度不能超过100个字符");
      }
      if (postCreateDTO.getContent() == null || postCreateDTO.getContent().isEmpty()) {
        return Result.fail(400, "内容不能为空");
      }
      if (postCreateDTO.getCategoryId() == null) {
        return Result.fail(400, "分类ID不能为空");
      }

      Post post = new Post();
      post.setTitle(postCreateDTO.getTitle());
      post.setContent(postCreateDTO.getContent());
      post.setCategoryId(postCreateDTO.getCategoryId());
      post.setUserId(userId);
      post.setCreateTime(postCreateDTO.getCreateTime());
      // 明确设置auditStatus为0，确保等待百度审核结果
      post.setAuditStatus(0);

      Post createdPost = postService.createPost(post, postCreateDTO.getImageUrls());
      Result<Post> result = Result.success(createdPost);
      result.setMsg("发布成功");
      return result;
    } catch (IllegalArgumentException e) {
      return Result.fail(401, e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "发布失败：" + e.getMessage());
    }
  }

  /**
   * 分页查询所有帖子
   */
  @GetMapping
  public Result<?> getAllPosts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestHeader(value = "Authorization", required = false) String authorization) {
    try {
      // 参数校验
      if (page < 0) {
        page = 0;
      }
      if (size < 1 || size > 100) {
        size = 10;
      }

      Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
      Page<Post> posts = postService.getAllPosts(pageable);

        Integer currentUserId = getUserIdFromToken(authorization);
        fillPostUserInfo(posts.getContent(), currentUserId);

      // 构建前端需要的完整分页响应格式
      Map<String, Object> pageResponse = new HashMap<>();
      pageResponse.put("content", posts.getContent());

      // 构建pageable对象
      Map<String, Object> pageableMap = new HashMap<>();
      pageableMap.put("pageNumber", page);
      pageableMap.put("pageSize", size);

      // 构建sort对象
      Map<String, Object> sortMap = new HashMap<>();
      sortMap.put("empty", posts.getSort().isEmpty());
      sortMap.put("sorted", posts.getSort().isSorted());
      sortMap.put("unsorted", posts.getSort().isUnsorted());

      pageableMap.put("sort", sortMap);
      pageableMap.put("offset", page * size);
      pageableMap.put("paged", true);
      pageableMap.put("unpaged", false);

      pageResponse.put("pageable", pageableMap);
      pageResponse.put("totalPages", posts.getTotalPages());
      pageResponse.put("totalElements", posts.getTotalElements());
      pageResponse.put("last", posts.isLast());
      pageResponse.put("size", posts.getSize());
      pageResponse.put("number", posts.getNumber());
      pageResponse.put("sort", sortMap);
      pageResponse.put("first", posts.isFirst());
      pageResponse.put("numberOfElements", posts.getNumberOfElements());
      pageResponse.put("empty", posts.isEmpty());

      return Result.success(pageResponse);
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "获取帖子列表失败");
    }
  }

  /**
   * 测试接口：查询所有帖子（不分页）
   */
  @GetMapping("/test")
  public Result<?> testGetAllPosts() {
    try {
      List<Post> posts = postService.getAllPostsWithoutPagination();
      return Result.success(posts);
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "测试查询失败：" + e.getMessage());
    }
  }

  /**
   * 按分类分页查询帖子
   */
  @GetMapping("/category/{categoryId}")
  public Result<Page<Post>> getPostsByCategory(
      @PathVariable Integer categoryId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestHeader(value = "Authorization", required = false) String authorization){
    try {
      if (categoryId == null) {
        return Result.fail(400, "分类ID不能为空");
      }
      Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
      Page<Post> posts = postService.getPostsByCategory(categoryId, pageable);

        Integer currentUserId = getUserIdFromToken(authorization);
        fillPostUserInfo(posts.getContent(), currentUserId);

      return Result.success(posts);
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "查询失败：" + e.getMessage());
    }
  }

  /**
   * 获取帖子详情（包含评论）
   */
  @GetMapping("/{id}")
  public Result<Post> getPostById(@PathVariable Integer id, @RequestHeader(value = "Authorization", required = false) String authorization) {
    try {
      if (id == null) {
        return Result.fail(400, "帖子ID不能为空");
      }
      // 使用PostService的getPostById方法获取帖子信息
      Post post = postService.getPostById(id);

        Integer currentUserId = getUserIdFromToken(authorization);
        fillPostUserInfo(List.of(post), currentUserId);

        if (post.getComments() != null && !post.getComments().isEmpty()) {
            fillCommentUserInfo(post.getComments(), currentUserId);
        }

      return Result.success(post);
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "查询失败：" + e.getMessage());
    }
  }

    private void fillPostUserInfo(List<Post> posts, Integer currentUserId) {
        if (posts == null || posts.isEmpty()) return;

        // 批量获取所有用户ID
        List<Integer> userIds = posts.stream()
                .map(Post::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户
        Map<Integer, String> userNames = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        for (Post post : posts) {
            // 设置用户名
            String userName = userNames.get(post.getUserId());
            post.setUserName(userName != null ? userName : "用户" + post.getUserId());

            // 设置是否可以删除
            post.setCanDelete(currentUserId != null && currentUserId.equals(post.getUserId()));
        }
    }

    // ✅ 添加评论填充方法
    private void fillCommentUserInfo(List<Comment> comments, Integer currentUserId) {
        if (comments == null || comments.isEmpty()) return;

        List<Integer> userIds = comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, String> userNames = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        for (Comment comment : comments) {
            String userName = userNames.get(comment.getUserId());
            comment.setUserName(userName != null ? userName : "用户" + comment.getUserId());
            comment.setCanDelete(currentUserId != null && currentUserId.equals(comment.getUserId()));
        }
    }

  /**
   * 删除帖子（只能本人删除）
   */
  @DeleteMapping("/{id}")
  public Result<?> deletePost(
      @PathVariable Integer id,
      @RequestHeader(value = "Authorization") String authorization) {
    try {
      // 从token解析用户ID
      Integer userId = getUserIdFromToken(authorization);
      
      if (id == null) {
        return Result.fail(400, "帖子ID不能为空");
      }
      postService.deletePost(id, userId);
      return Result.success(null);
    } catch (IllegalArgumentException e) {
      return Result.fail(401, e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "删除失败：" + e.getMessage());
    }
  }

  private Integer getUserIdFromToken(String authorization) {
    return jwtUtil.getUserIdFromToken(jwtUtil.extractToken(authorization));
  }

    public CommentService getCommentService() {
        return commentService;
    }

    public PostRepository getPostRepository() {
        return postRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}