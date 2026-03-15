package com.campus.forum.sports.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "venues")
public class Venue {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "venue_type", nullable = false)
  private String venueType;

  @Column(name = "venue_name", nullable = false)
  private String venueName;

  @Column(name = "total_courts", nullable = false)
  private Integer totalCourts;

  @Column(name = "description")
  private String description;

  @Column(name = "created_at")
  private java.time.LocalDateTime createdAt;

  @Column(name = "updated_at")
  private java.time.LocalDateTime updatedAt;
}