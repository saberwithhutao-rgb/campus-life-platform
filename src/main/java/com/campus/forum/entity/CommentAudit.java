package com.campus.forum.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论审核记录实体类
 */
@Data
@Entity
@Table(name = "comment_audit")
public class CommentAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 审核结果：0-待审核，1-正常，2-违规
     */
    private Integer auditStatus;

    /**
     * 违规详情
     */
    @Column(columnDefinition = "TEXT")
    private String violationDetails;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
