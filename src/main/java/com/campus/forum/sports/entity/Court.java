package com.campus.forum.sports.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "courts")
public class Court {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "venue_id", nullable = false)
  private Integer venueId;

  @Column(name = "court_code", nullable = false)
  private String courtCode;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "created_at")
  private java.time.LocalDateTime createdAt;

  @Column(name = "updated_at")
  private java.time.LocalDateTime updatedAt;

  @ManyToOne
  @JoinColumn(name = "venue_id", insertable = false, updatable = false)
  private Venue venue;
}