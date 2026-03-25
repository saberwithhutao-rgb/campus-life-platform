package com.campus.forum.repository;

import com.campus.forum.entity.PostAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 帖子审核记录Repository
 */
@Repository
public interface PostAuditRepository extends JpaRepository<PostAudit, Long> {

    /**
     * 根据帖子ID查询审核记录
     * @param postId 帖子ID
     * @return 审核记录
     */
    PostAudit findByPostId(Long postId);
}
