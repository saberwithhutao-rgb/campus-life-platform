package com.school.test;

import com.school.entity.StudyPlan;
import com.school.service.StudyPlanService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StudyPlanTest {

  private static final String[] DIFFICULTY_LEVELS = { "easy", "medium", "hard" };
  private static final String[] PLAN_TYPES = { "daily", "weekly", "monthly", "semester" };
  private static final String[] SUBJECTS = { "Mathematics", "English", "Chinese", "Physics", "Chemistry", "Biology",
      "History", "Geography", "Computer Science" };
  private static final String[] STATUSES = { "active", "completed", "paused" };

  public static void main(String[] args) {
    // 生成10条随机测试数据
    List<StudyPlan> studyPlans = generateTestData(10);

    // 打印生成的数据
    System.out.println("Generated test data:");
    for (StudyPlan plan : studyPlans) {
      System.out
          .println("ID: " + plan.getId() + ", Title: " + plan.getTitle() + ", Difficulty: " + plan.getDifficulty());
    }

    // 插入数据到数据库
    ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
    StudyPlanService studyPlanService = context.getBean(StudyPlanService.class);

    try {
      studyPlanService.batchInsert(studyPlans);
      System.out.println("Data inserted successfully!");
    } catch (Exception e) {
      System.out.println("Error inserting data: " + e.getMessage());
      e.printStackTrace();
    } finally {
      ((ClassPathXmlApplicationContext) context).close();
    }
  }

  private static List<StudyPlan> generateTestData(int count) {
    List<StudyPlan> studyPlans = new ArrayList<>();
    Random random = new Random();

    for (int i = 1; i <= count; i++) {
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