package com.campus.forum.library.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "classrooms")
public class Classroom {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "floor_id", nullable = false)
  private Integer floorId;

  @Column(name = "classroom_name", nullable = false)
  private String classroomName;

  @Column(name = "seat_count", nullable = false)
  private Integer seatCount;

  @ManyToOne
  @JoinColumn(name = "floor_id", insertable = false, updatable = false)
  private Floor floor;
}
