package com.campus.forum.library.service.impl;

import com.campus.forum.library.entity.Classroom;
import com.campus.forum.library.repository.ClassroomRepository;
import com.campus.forum.library.service.ClassroomService;
import com.campus.forum.library.dto.AvailableSeatsDTO;
import com.campus.forum.library.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class ClassroomServiceImpl implements ClassroomService {
  private final ClassroomRepository classroomRepository;
  private final ReservationRepository reservationRepository;

  public ClassroomServiceImpl(ClassroomRepository classroomRepository, ReservationRepository reservationRepository) {
    this.classroomRepository = classroomRepository;
    this.reservationRepository = reservationRepository;
  }

  @Override
  public List<Classroom> getClassroomsByFloorId(Integer floorId) {
    return classroomRepository.findByFloorId(floorId);
  }

  @Override
  public AvailableSeatsDTO getAvailableSeatsByClassroomId(Integer classroomId) {
    // 获取教室信息
    Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new RuntimeException("教室不存在"));
    int totalSeats = classroom.getSeatCount();
    
    // 获取当前系统时间
    LocalTime currentTime = LocalTime.now();
    LocalDate today = LocalDate.now();
    
    // 查询该教室在今天且当前时间正在使用中的预约（status = 'active'）
    int usedSeats = reservationRepository.countDistinctSeatByClassroomIdAndReserveDateAndStatusAndCurrentTime(
            classroomId, today, "active", currentTime);
    
    return new AvailableSeatsDTO(totalSeats, usedSeats);
  }
}
