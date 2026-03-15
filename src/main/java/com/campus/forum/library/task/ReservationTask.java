package com.campus.forum.library.task;

import com.campus.forum.library.service.ReservationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationTask {
  private final ReservationService reservationService;

  public ReservationTask(ReservationService reservationService) {
    this.reservationService = reservationService;
  }

  /**
   * 每分钟执行一次，处理到期自动结束的预约
   */
  @Scheduled(cron = "0 * * * * *")
  public void processExpiredReservations() {
    reservationService.processExpiredReservations();
  }
}
