package com.campus.forum.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "post")
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "category_id", nullable = false)
  private Integer categoryId;

  @Column(name = "user_id", nullable = false)
  private Integer userId;

  @Column(name = "create_time")
  private LocalDateTime createTime;

  @Column(name = "audit_status", columnDefinition = "INT DEFAULT 0")
  private Integer auditStatus;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @org.hibernate.annotations.BatchSize(size = 10)
  private List<Comment> comments;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<PostImage> images;

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