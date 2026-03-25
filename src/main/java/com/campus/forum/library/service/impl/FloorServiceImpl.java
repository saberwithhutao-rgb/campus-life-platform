package com.campus.forum.library.service.impl;

import com.campus.forum.library.entity.Floor;
import com.campus.forum.library.repository.FloorRepository;
import com.campus.forum.library.service.FloorService;
import com.campus.forum.common.util.RedisCacheUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.util.List;

@Service
public class FloorServiceImpl implements FloorService {
  private final FloorRepository floorRepository;

  @Resource
  private RedisCacheUtil redisCacheUtil;

  public FloorServiceImpl(FloorRepository floorRepository) {
    this.floorRepository = floorRepository;
  }

  @Override
  public List<Floor> getAllFloors() {
    String cacheKey = "library:floors:all";
    List<Floor> floors = redisCacheUtil.getCache(cacheKey);
    if (floors == null) {
      floors = floorRepository.findAll();
      redisCacheUtil.setCache(cacheKey, floors, 3600);
    }
    return floors;
  }
}
