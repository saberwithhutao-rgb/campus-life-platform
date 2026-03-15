package com.campus.forum.library.service.impl;

import com.campus.forum.library.entity.Floor;
import com.campus.forum.library.repository.FloorRepository;
import com.campus.forum.library.service.FloorService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FloorServiceImpl implements FloorService {
  private final FloorRepository floorRepository;

  public FloorServiceImpl(FloorRepository floorRepository) {
    this.floorRepository = floorRepository;
  }

  @Override
  public List<Floor> getAllFloors() {
    return floorRepository.findAll();
  }
}
