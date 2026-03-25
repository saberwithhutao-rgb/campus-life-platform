package com.campus.forum.sports.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "venue_reservations")
public class VenueReservation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "user_id", nullable = false)
  private Integer userId;

  @Column(name = "court_id", nullable = false)
  private Integer courtId;

  @Column(name = "venue_id", nullable = false)
  private Integer venueId;

  @Column(name = "reserve_date", nullable = false)
  private java.time.LocalDate reserveDate;

  @Column(name = "start_time", nullable = false)
  private java.time.LocalTime startTime;

  @Column(name = "duration", nullable = false)
  private Integer duration;

  @Column(name = "end_time", nullable = false)
  private java.time.LocalTime endTime;

  @Column(name = "type", nullable = false)
  private String type;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "created_at")
  private java.time.LocalDateTime createdAt;

  @Column(name = "actual_end_time")
  private java.time.LocalDateTime actualEndTime;

  @Column(name = "actual_duration")
  private Integer actualDuration;

  @ManyToOne
  @JoinColumn(name = "court_id", insertable = false, updatable = false)
  private Court court;

  @ManyToOne
  @JoinColumn(name = "venue_id", insertable = false, updatable = false)
  private Venue venue;

  @Version
  @Column(name = "version")
  private Integer version = 1;

    @Transient
    private String userName;

    @Transient
    private Boolean isOwner;
}