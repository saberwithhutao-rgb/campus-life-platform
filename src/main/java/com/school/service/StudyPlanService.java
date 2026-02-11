package com.school.service;

import com.school.entity.StudyPlan;

import java.util.List;

public interface StudyPlanService {
  void batchInsert(List<StudyPlan> studyPlans);
}