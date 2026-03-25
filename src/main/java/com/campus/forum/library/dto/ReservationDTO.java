package com.campus.forum.library.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReservationDTO {
  private Integer seatId;
  private Integer classroomId;
  private LocalDate reserveDate;
  private LocalTime startTime;
  private Integer duration;
  private LocalTime endTime;
  private String type;
}