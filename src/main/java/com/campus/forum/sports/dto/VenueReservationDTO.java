package com.campus.forum.sports.dto;

import lombok.Data;

@Data
public class VenueReservationDTO {
  private Integer userId;
  private Integer courtId;
  private Integer venueId;
  private String reserveDate;
  private String startTime;
  private Integer duration;
  private String endTime;
}