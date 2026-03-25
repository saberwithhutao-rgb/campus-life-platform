package com.campus.forum.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "comment_image")
public class CommentImage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "comment_id", nullable = false)
  private Integer commentId;

  @Column(name = "image_url", nullable = false)
  private String imageUrl;

  @ManyToOne
  @JoinColumn(name = "comment_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private Comment comment;
}