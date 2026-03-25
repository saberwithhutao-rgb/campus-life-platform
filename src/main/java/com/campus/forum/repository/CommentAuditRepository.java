package com.campus.forum.repository;

import com.campus.forum.entity.CommentAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 评论审核记录Repository
 */
@Repository
public interface CommentAuditRepository extends JpaRepository<CommentAudit, Long> {

    /**
     * 根据评论ID查询审核记录
     * @param commentId 评论ID
     * @return 审核记录
     */
    CommentAudit findByCommentId(Long commentId);
}
