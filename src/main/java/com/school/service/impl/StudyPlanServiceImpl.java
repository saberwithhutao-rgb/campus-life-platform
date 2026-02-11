package com.school.service.impl;

import com.school.entity.StudyPlan;
import com.school.mapper.StudyPlanMapper;
import com.school.service.StudyPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudyPlanServiceImpl implements StudyPlanService {

  @Autowired
  private StudyPlanMapper studyPlanMapper;

  @Override
  @Transactional
  public void batchInsert(List<StudyPlan> studyPlans) {
    studyPlanMapper.insertBatch(studyPlans);
  }
}