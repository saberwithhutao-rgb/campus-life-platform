package com.campus.forum.sports.task;

import com.campus.forum.sports.service.VenueReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SportsReservationTask {

  @Autowired
  private VenueReservationService reservationService;

  @Scheduled(cron = "0 * * * * *") // 每分钟执行一次
  public void processExpiredReservations() {
    reservationService.processExpiredReservations();
  }
}