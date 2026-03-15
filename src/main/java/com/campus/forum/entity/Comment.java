package com.campus.forum.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

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

  @ManyToOne
  @JoinColumn(name = "post_id", insertable = false, updatable = false)
  @JsonIgnore
  private Post post;
}