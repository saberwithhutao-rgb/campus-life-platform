package com.campus.forum.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "comment")
public class Comment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "post_id", nullable = false)
  private Integer postId;

  @Column(name = "user_id", nullable = false)
  private Integer userId;

  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "create_time")
  private LocalDateTime createTime;

  @Column(name = "audit_status", columnDefinition = "INT DEFAULT 0")
  private Integer auditStatus;

  @ManyToOne
  @JoinColumn(name = "post_id", insertable = false, updatable = false)
  @JsonIgnore
  private Post post;

  @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @org.hibernate.annotations.BatchSize(size = 10)
  private List<CommentImage> images = new java.util.ArrayList<>();

    @Transient
    private String userName;

    @Transient
    private String userAvatar;

    @Transient
    private Boolean canDelete;

  @PrePersist
  public void prePersist() {
    if (auditStatus == null) {
      auditStatus = 0;
    }
  }
}