package com.school.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Random;

/**
 * 学习计划数据初始化器
 * 项目启动时自动执行，插入符合要求的测试数据
 */
@Component
public class StudyPlanDataInitializer implements ApplicationRunner {

        @Autowired
        private JdbcTemplate jdbcTemplate;
        private final Random random = new Random();

        @Override
        public void run(ApplicationArguments args) throws Exception {
                System.out.println("开始初始化学习计划测试数据...");

                // 1. 清空原有数据
                clearExistingData();

                // 2. 插入新数据
                insertTestData();

                System.out.println("学习计划测试数据初始化完成！");
        }

        /**
         * 清空原有数据
         */
        private void clearExistingData() {
                try {
                        jdbcTemplate.execute("DELETE FROM public.study_plans");
                        System.out.println("已清空原有学习计划数据");
                } catch (Exception e) {
                        System.out.println("清空数据时发生异常: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        /**
         * 插入测试数据
         */
        private void insertTestData() {
                int id = 1;
                LocalDate today = LocalDate.now();

                // 2.1 今天的任务（已完成3条，未完成2条）
                String[] todayTitles = {
                                "完成数学作业", "复习英语单词", "阅读教材第三章", "准备考试", "整理笔记"
                };
                String[] todaySubjects = { "数学", "英语", "语文", "物理", "化学" };
                String[] todayDifficulties = { "easy", "medium", "hard", "medium", "easy" };
                String[] todayStatuses = { "已完成", "已完成", "已完成", "未完成", "未完成" };

                for (int i = 0; i < todayTitles.length; i++) {
                        LocalDate startDate = today.minusDays(1);
                        String status = todayStatuses[i];
                        int progressPercent = status.equals("已完成") ? 100 : random.nextInt(80) + 10; // 未完成的进度10-89%
                        insertStudyPlan(id++, 1, todayTitles[i], "今日学习任务：" + todayTitles[i],
                                        "daily", todaySubjects[i], todayDifficulties[i], status, progressPercent,
                                        startDate, today);
                }

                // 2.2 过去一周的任务（已完成8条，未完成5条）
                String[] weekTitles = {
                                "完成数学练习册", "复习英语语法", "阅读课外书籍", "做物理实验", "化学公式记忆",
                                "历史事件梳理", "地理地图背诵", "生物知识点总结", "政治理论学习", "体育锻炼计划",
                                "编程练习", "英语听力", "写作练习"
                };
                String[] weekSubjects = { "数学", "英语", "语文", "物理", "化学", "历史", "地理", "生物", "政治", "体育", "计算机", "英语",
                                "语文" };
                String[] weekDifficulties = { "medium", "hard", "easy", "medium", "hard", "easy", "medium", "hard",
                                "easy",
                                "medium", "hard", "medium", "easy" };
                String[] weekStatuses = { "已完成", "已完成", "已完成", "已完成", "已完成",
                                "已完成", "已完成", "已完成", "未完成", "未完成", "未完成", "未完成", "未完成" };

                for (int i = 0; i < weekTitles.length; i++) {
                        LocalDate endDate = today.minusDays(1 + i % 6); // 过去1-6天
                        LocalDate startDate = endDate.minusDays(2);
                        String status = weekStatuses[i];
                        int progressPercent = status.equals("已完成") ? 100 : random.nextInt(70) + 20; // 未完成的进度20-89%
                        insertStudyPlan(id++, 1, weekTitles[i], "周学习任务：" + weekTitles[i],
                                        "weekly", weekSubjects[i], weekDifficulties[i], status, progressPercent,
                                        startDate, endDate);
                }

                // 2.3 过去一个月的任务（已完成12条，未完成8条）
                String[] monthTitles = {
                                "完成第一单元测试", "复习第一章内容", "准备演讲比赛", "做模拟试题", "整理错题本",
                                "参加辅导课程", "完成项目作业", "学习新知识点", "复习旧知识", "做练习题",
                                "阅读专业书籍", "写学习总结", "制定学习计划", "参加小组讨论", "准备期中考试",
                                "编程项目", "英语写作", "数学建模", "物理竞赛准备", "化学实验报告"
                };
                String[] monthSubjects = { "数学", "英语", "语文", "物理", "化学", "历史", "地理", "生物", "政治", "数学",
                                "英语", "语文", "物理", "化学", "英语", "计算机", "英语", "数学", "物理", "化学" };
                String[] monthDifficulties = { "hard", "medium", "easy", "hard", "medium", "easy", "hard", "medium",
                                "easy",
                                "hard", "medium", "easy", "hard", "medium", "easy", "hard", "medium", "hard", "medium",
                                "medium" };
                String[] monthStatuses = { "已完成", "已完成", "已完成", "已完成", "已完成",
                                "已完成", "已完成", "已完成", "已完成", "已完成",
                                "已完成", "已完成", "未完成", "未完成", "未完成",
                                "未完成", "未完成", "未完成", "未完成", "未完成" };

                for (int i = 0; i < monthTitles.length; i++) {
                        LocalDate endDate = today.minusDays(7 + i % 23); // 过去7-29天
                        LocalDate startDate = endDate.minusDays(3);
                        String status = monthStatuses[i];
                        int progressPercent = status.equals("已完成") ? 100 : random.nextInt(60) + 30; // 未完成的进度30-89%
                        insertStudyPlan(id++, 1, monthTitles[i], "月学习任务：" + monthTitles[i],
                                        "monthly", monthSubjects[i], monthDifficulties[i], status, progressPercent,
                                        startDate, endDate);
                }
        }

        /**
         * 插入单条学习计划数据
         */
        private void insertStudyPlan(
                        int id, int userId, String title, String description,
                        String planType, String subject, String difficulty,
                        String status, int progressPercent, LocalDate startDate, LocalDate endDate) {

                try {
                        String sql = "INSERT INTO public.study_plans (id, user_id, title, description, plan_type, subject, difficulty, status, progress_percent, start_date, end_date, created_at, updated_at) "
                                        +
                                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

                        jdbcTemplate.update(sql,
                                        id, userId, title, description, planType, subject, difficulty, status,
                                        progressPercent, startDate, endDate);

                } catch (Exception e) {
                        System.out.println("插入数据时发生异常: " + e.getMessage());
                        e.printStackTrace();
                }
        }
}
