package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.common.annotation.CurrentUser;
import com.campus.forum.entity.Post;
import com.campus.forum.service.PostService;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/forum/posts")
public class PostController {

  private final PostService postService;

  public PostController(PostService postService) {
    this.postService = postService;
  }

  /**
   * 发布帖子
   */
  @PostMapping
  public Result<Post> createPost(@RequestBody Post post, @CurrentUser Integer userId) {
    try {
      // 参数校验
      if (post.getTitle() == null || post.getTitle().isEmpty()) {
        return Result.fail(400, "标题不能为空");
      }
      if (post.getTitle().length() > 100) {
        return Result.fail(400, "标题长度不能超过100个字符");
      }
      if (post.getContent() == null || post.getContent().isEmpty()) {
        return Result.fail(400, "内容不能为空");
      }
      if (post.getCategoryId() == null) {
        return Result.fail(400, "分类ID不能为空");
      }

      post.setUserId(userId);
      Post createdPost = postService.createPost(post);
      Result<Post> result = Result.success(createdPost);
      result.setMsg("发布成功");
      return result;
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
      @RequestParam(defaultValue = "10") int size) {
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
      @RequestParam(defaultValue = "10") int size) {
    try {
      if (categoryId == null) {
        return Result.fail(400, "分类ID不能为空");
      }
      Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
      Page<Post> posts = postService.getPostsByCategory(categoryId, pageable);
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
  public Result<Post> getPostById(@PathVariable Integer id) {
    try {
      if (id == null) {
        return Result.fail(400, "帖子ID不能为空");
      }
      Post post = postService.getPostById(id);
      return Result.success(post);
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "查询失败：" + e.getMessage());
    }
  }

  /**
   * 删除帖子（只能本人删除）
   */
  @DeleteMapping("/{id}")
  public Result<?> deletePost(
      @PathVariable Integer id,
      @CurrentUser Integer userId) {
    try {
      if (id == null) {
        return Result.fail(400, "帖子ID不能为空");
      }
      postService.deletePost(id, userId);
      return Result.success(null);
    } catch (Exception e) {
      e.printStackTrace();
      return Result.fail(500, "删除失败：" + e.getMessage());
    }
  }
}