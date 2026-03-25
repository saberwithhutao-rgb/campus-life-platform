package com.campus.forum.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reservations")
public class Reservation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "user_id", nullable = false)
  private Integer userId;

  @Column(name = "seat_id", nullable = false)
  private Integer seatId;

  @Column(name = "classroom_id", nullable = false)
  private Integer classroomId;

  @Column(name = "reserve_date", nullable = false)
  private LocalDate reserveDate;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "duration", nullable = false)
  private Integer duration;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  @Column(name = "type", nullable = false)
  private String type; // reservation, occupation

  @Column(name = "status", nullable = false)
  private String status; // active, completed, cancelled, replaced

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "actual_end_time")
  private LocalDateTime actualEndTime;

  @Column(name = "actual_duration_minutes")
  private Integer actualDurationMinutes;

  @ManyToOne
  @JoinColumn(name = "seat_id", insertable = false, updatable = false)
  private Seat seat;

  @ManyToOne
  @JoinColumn(name = "classroom_id", insertable = false, updatable = false)
  private Classroom classroom;

  @Version
  @Column(name = "version")
  private Integer version = 1;
}