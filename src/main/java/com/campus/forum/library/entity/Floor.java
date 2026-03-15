package com.campus.forum.library.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "floors")
public class Floor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "floor_num", nullable = false, unique = true)
  private Integer floorNum;

  @Column(name = "description")
  private String description;
}
