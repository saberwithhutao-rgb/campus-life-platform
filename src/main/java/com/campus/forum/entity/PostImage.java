package com.campus.forum.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "post_image")
public class PostImage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "post_id", nullable = false)
  private Integer postId;

  @Column(name = "image_url", nullable = false)
  private String imageUrl;

  @ManyToOne
  @JoinColumn(name = "post_id", insertable = false, updatable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private Post post;
}