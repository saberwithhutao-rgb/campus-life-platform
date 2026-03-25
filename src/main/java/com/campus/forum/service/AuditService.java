package com.campus.forum.service;

import com.campus.forum.entity.*;
import com.campus.forum.repository.*;
import com.campus.forum.util.BaiduAuditUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 异步审核服务类
 */
@Service
public class AuditService {

  private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

  @Autowired
  private BaiduAuditUtil baiduAuditUtil;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private PostImageRepository postImageRepository;

  @Autowired
  private PostAuditRepository postAuditRepository;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private CommentImageRepository commentImageRepository;

  @Autowired
  private CommentAuditRepository commentAuditRepository;

  /**
   * 异步审核帖子
   * 
   * @param postId 帖子ID
   */
  @Async("auditExecutor")
  public void auditPost(Long postId) {
    int finalAuditStatus = 0; // 默认未审核
    boolean hasViolation = false;
    boolean hasAuditFailure = false;
    StringBuilder violationDetails = new StringBuilder();

    try {
      logger.info("开始审核帖子: {}", postId);

      // 查询帖子信息
      Post post = postRepository.findById(postId.intValue()).orElse(null);
      if (post == null) {
        logger.error("帖子不存在: {}", postId);
        return;
      }

      // 审核帖子标题和内容

      // 合并标题和内容成一段文本，减少 API 调用次数，避免 QPS 超限
      String combinedText = post.getTitle() + " " + post.getContent();
      logger.info("开始审核帖子标题和内容: {}", combinedText);
      JSONObject combinedResult = baiduAuditUtil.auditText(combinedText);
      logger.info("百度完整返回结果: {}", combinedResult);
      int combinedStatus = baiduAuditUtil.getAuditStatus(combinedResult);
      logger.info("标题和内容审核状态: {}", combinedStatus);
      if (combinedStatus == 2) {
        hasViolation = true;
        violationDetails.append("标题或内容违规: ").append(baiduAuditUtil.getViolationDetails(combinedResult)).append("\n");
      } else if (combinedStatus == 1) {
        // 标题和内容合规，暂时保持状态
      } else if (combinedStatus == 3) {
        hasAuditFailure = true; // 审核失败
      }

      // 审核帖子图片
      List<PostImage> postImages = postImageRepository.findByPostId(postId.intValue());
      logger.info("开始审核帖子图片，数量: {}", postImages.size());
      for (PostImage image : postImages) {
        logger.info("审核图片URL: {}", image.getImageUrl());
        JSONObject imageResult = baiduAuditUtil.auditImage(image.getImageUrl());
        logger.info("图片审核结果: {}", imageResult);
        int imageStatus = baiduAuditUtil.getAuditStatus(imageResult);
        if (imageStatus == 2) {
          hasViolation = true;
          violationDetails.append("图片违规: " + baiduAuditUtil.getViolationDetails(imageResult) + "\n");
        } else if (imageStatus == 1) {
          // 图片合规，暂时保持状态
        } else if (imageStatus == 3) {
          hasAuditFailure = true; // 审核失败
        }
      }

      // 确定最终状态
      if (hasViolation) {
        finalAuditStatus = 2; // 有违规内容
        logger.info("检测到违规内容，设置最终状态为: 2 (违规)");
      } else if (hasAuditFailure) {
        finalAuditStatus = 3; // 审核失败
        logger.info("检测到审核失败，设置最终状态为: 3 (审核失败)");
      } else {
        finalAuditStatus = 1; // 全部合规
        logger.info("所有内容合规，设置最终状态为: 1 (正常)");
      }

      // 创建或更新审核记录
      PostAudit postAudit = postAuditRepository.findByPostId(postId);
      if (postAudit == null) {
        postAudit = new PostAudit();
        postAudit.setPostId(postId);
        logger.info("创建新的审核记录");
      } else {
        logger.info("更新现有审核记录");
      }

      logger.info("审核完成，帖子ID: {}, 最终状态: {}, hasViolation: {}", postId, finalAuditStatus, hasViolation);

      // 更新帖子状态
      logger.info("开始更新帖子状态，当前状态: {}, 新状态: {}", post.getAuditStatus(), finalAuditStatus);
      post.setAuditStatus(finalAuditStatus);
      logger.info("帖子状态设置完成: {}", post.getAuditStatus());

      logger.info("开始保存帖子状态");
      Post savedPost = postRepository.save(post);
      logger.info("帖子保存完成，保存后的状态: {}", savedPost.getAuditStatus());

      postAudit.setAuditStatus(finalAuditStatus);
      postAudit.setViolationDetails(hasViolation ? violationDetails.toString() : "");

      logger.info("开始保存审核记录");
      PostAudit savedPostAudit = postAuditRepository.save(postAudit);
      logger.info("审核记录保存完成，状态: {}", savedPostAudit.getAuditStatus());

      logger.info("帖子审核完成: {}, 最终状态: {}", postId, savedPost.getAuditStatus());

    } catch (Exception e) {
      logger.error("审核帖子失败: {}", e.getMessage(), e);
      // 审核失败，更新状态为3，但如果已经检测到违规，状态设为2
      try {
        Post post = postRepository.findById(postId.intValue()).orElse(null);
        if (post != null) {
          int errorStatus = hasViolation ? 2 : 3;
          post.setAuditStatus(errorStatus);
          postRepository.save(post);

          PostAudit postAudit = postAuditRepository.findByPostId(postId);
          if (postAudit == null) {
            postAudit = new PostAudit();
            postAudit.setPostId(postId);
          }
          postAudit.setAuditStatus(errorStatus);
          postAudit.setViolationDetails(
              hasViolation ? (!violationDetails.isEmpty() ? violationDetails.toString() : "检测到违规，但审核过程出错")
                  : "审核失败: " + e.getMessage());
          postAuditRepository.save(postAudit);

          logger.info("帖子审核异常，更新状态为{}: {}, hasViolation: {}", errorStatus, postId, hasViolation);
        }
      } catch (Exception ex) {
        logger.error("更新审核失败状态失败: {}", ex.getMessage(), ex);
      }
    }
  }

  /**
   * 异步审核评论
   * 
   * @param commentId 评论ID
   */
  @Async("auditExecutor")
  public void auditComment(Long commentId) {
    int finalAuditStatus = 0; // 默认未审核
    boolean hasViolation = false;
    boolean hasAuditFailure = false;
    StringBuilder violationDetails = new StringBuilder();
    
    try {
      logger.info("开始审核评论: {}", commentId);

      // 查询评论信息
      Comment comment = commentRepository.findById(commentId.intValue()).orElse(null);
      if (comment == null) {
        logger.error("评论不存在: {}", commentId);
        return;
      }

      // 审核评论内容

      // 审核内容
      logger.info("开始审核评论内容");
      JSONObject contentResult = baiduAuditUtil.auditText(comment.getContent());
      logger.info("内容审核结果: {}", contentResult);
      int contentStatus = baiduAuditUtil.getAuditStatus(contentResult);
      if (contentStatus == 2) {
        hasViolation = true;
        violationDetails.append("内容违规: ").append(baiduAuditUtil.getViolationDetails(contentResult)).append("\n");
      } else if (contentStatus == 1) {
        // 内容合规，暂时保持状态
      } else if (contentStatus == 3) {
        hasAuditFailure = true; // 审核失败
      }

      // 审核评论图片
      List<CommentImage> commentImages = commentImageRepository.findByCommentId(commentId.intValue());
      logger.info("开始审核评论图片，数量: {}", commentImages.size());
      for (CommentImage image : commentImages) {
        logger.info("审核图片URL: {}", image.getImageUrl());
        JSONObject imageResult = baiduAuditUtil.auditImage(image.getImageUrl());
        logger.info("图片审核结果: {}", imageResult);
        int imageStatus = baiduAuditUtil.getAuditStatus(imageResult);
        if (imageStatus == 2) {
          hasViolation = true;
          violationDetails.append("图片违规: ").append(baiduAuditUtil.getViolationDetails(imageResult)).append("\n");
        } else if (imageStatus == 1) {
          // 图片合规，暂时保持状态
        } else if (imageStatus == 3) {
          hasAuditFailure = true; // 审核失败
        }
      }

      // 确定最终状态
      if (hasViolation) {
        finalAuditStatus = 2; // 有违规内容
      } else if (hasAuditFailure) {
        finalAuditStatus = 3; // 审核失败
      } else {
        finalAuditStatus = 1; // 全部合规
      }

      // 创建或更新审核记录
      CommentAudit commentAudit = commentAuditRepository.findByCommentId(commentId);
      if (commentAudit == null) {
        commentAudit = new CommentAudit();
        commentAudit.setCommentId(commentId);
      }


      
      // 更新评论状态
      comment.setAuditStatus(finalAuditStatus);
      commentRepository.save(comment);
      
      commentAudit.setAuditStatus(finalAuditStatus);
      commentAudit.setViolationDetails(hasViolation ? violationDetails.toString() : "");

      commentAuditRepository.save(commentAudit);
      logger.info("评论审核完成: {}, 最终状态: {}", commentId, comment.getAuditStatus());

    } catch (Exception e) {
      logger.error("审核评论失败: {}", e.getMessage(), e);
      // 审核失败，更新状态为3，但如果已经检测到违规，状态设为2
      try {
        Comment comment = commentRepository.findById(commentId.intValue()).orElse(null);
        if (comment != null) {
          int errorStatus = hasViolation ? 2 : 3;
          comment.setAuditStatus(errorStatus);
          commentRepository.save(comment);

          CommentAudit commentAudit = commentAuditRepository.findByCommentId(commentId);
          if (commentAudit == null) {
            commentAudit = new CommentAudit();
            commentAudit.setCommentId(commentId);
          }
          commentAudit.setAuditStatus(errorStatus);
          commentAudit.setViolationDetails(hasViolation ? 
              (!violationDetails.isEmpty() ? violationDetails.toString() : "检测到违规，但审核过程出错") :
              "审核失败: " + e.getMessage());
          commentAuditRepository.save(commentAudit);

          logger.info("评论审核异常，更新状态为{}: {}, hasViolation: {}", errorStatus, commentId, hasViolation);
        }
      } catch (Exception ex) {
        logger.error("更新审核失败状态失败: {}", ex.getMessage(), ex);
      }
    }
  }
}
