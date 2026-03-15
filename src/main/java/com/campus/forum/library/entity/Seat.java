package com.campus.forum.library.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "seats")
public class Seat {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "classroom_id", nullable = false)
  private Integer classroomId;

  @Column(name = "seat_code", nullable = false)
  private String seatCode;

  @Column(name = "status", nullable = false)
  private String status; // available, reserved, occupied

  @ManyToOne
  @JoinColumn(name = "classroom_id", insertable = false, updatable = false)
  private Classroom classroom;
}