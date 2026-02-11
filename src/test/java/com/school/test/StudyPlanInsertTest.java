package com.school.test;

import com.school.entity.StudyPlan;
import com.school.service.StudyPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest
public class StudyPlanInsertTest {

  private static final String[] DIFFICULTY_LEVELS = { "easy", "medium", "hard" };
  private static final String[] PLAN_TYPES = { "daily", "weekly", "monthly", "semester" };
  private static final String[] SUBJECTS = { "Mathematics", "English", "Chinese", "Physics", "Chemistry", "Biology",
      "History", "Geography", "Computer Science" };
  private static final String[] STATUSES = { "active", "completed", "paused" };

  @Autowired
  private StudyPlanService studyPlanService;

  @Test
  public void testBatchInsert() {
    // 生成20条随机测试数据
    List<StudyPlan> studyPlans = generateTestData(20);

    // 打印生成的数据
    System.out.println("Generated test data:");
    for (StudyPlan plan : studyPlans) {
      System.out
          .println("ID: " + plan.getId() + ", Title: " + plan.getTitle() + ", Difficulty: " + plan.getDifficulty());
    }

    // 插入数据到数据库
    try {
      studyPlanService.batchInsert(studyPlans);
      System.out.println("Data inserted successfully!");
    } catch (Exception e) {
      System.out.println("Error inserting data: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private List<StudyPlan> generateTestData(int count) {
    List<StudyPlan> studyPlans = new ArrayList<>();
    Random random = new Random();

    // 从11开始，避免与之前的10条数据冲突
    for (int i = 11; i <= 10 + count; i++) {
      StudyPlan plan = new StudyPlan();
      plan.setId(i);
      plan.setUserId(random.nextInt(100) + 1);
      plan.setTitle("Study Plan " + i + " - " + SUBJECTS[random.nextInt(SUBJECTS.length)]);
      plan.setDescription("This is a " + DIFFICULTY_LEVELS[random.nextInt(DIFFICULTY_LEVELS.length)]
          + " difficulty study plan for " + SUBJECTS[random.nextInt(SUBJECTS.length)]);
      plan.setPlanType(PLAN_TYPES[random.nextInt(PLAN_TYPES.length)]);
      plan.setSubject(SUBJECTS[random.nextInt(SUBJECTS.length)]);
      plan.setDifficulty(DIFFICULTY_LEVELS[random.nextInt(DIFFICULTY_LEVELS.length)]);
      plan.setStatus(STATUSES[random.nextInt(STATUSES.length)]);
      plan.setProgressPercent((short) (random.nextInt(101)));
      plan.setStartDate(LocalDate.now().minusDays(random.nextInt(30)));
      plan.setEndDate(LocalDate.now().plusDays(random.nextInt(90)));
      studyPlans.add(plan);
    }

    return studyPlans;
  }
}