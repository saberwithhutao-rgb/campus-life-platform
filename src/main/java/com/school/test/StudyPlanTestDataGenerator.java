package com.school.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Random;

/**
 * 学习计划测试数据生成器
 * 生成20条随机测试数据，其中10条end_date为今天
 * 包含已完成和未完成的计划
 */
@Component
public class StudyPlanTestDataGenerator {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Random random = new Random();

    private static final String[] DIFFICULTY_LEVELS = { "easy", "medium", "hard" };
    private static final String[] PLAN_TYPES = { "daily", "weekly", "monthly", "project" };
    private static final String[] SUBJECTS = { "数学", "英语", "语文", "物理", "化学", "生物", "历史", "地理", "计算机" };
    private static final String[] STATUSES = { "已完成", "未完成" };
    private static final String[] TITLE_PREFIXES = {
            "完成", "复习", "学习", "准备", "整理", "阅读", "练习", "参加", "制定", "研究"
    };
    private static final String[] TITLE_SUFFIXES = {
            "作业", "笔记", "教材", "考试", "实验", "项目", "论文", "演讲", "计划", "知识点"
    };

    /**
     * 生成并插入20条测试数据
     */
    public void generateAndInsertTestData() {
        System.out.println("开始生成学习计划测试数据...");

        // 清空原有数据
        clearExistingData();

        LocalDate today = LocalDate.now();
        int id = 1;

        // 生成10条今天结束的计划
        for (int i = 0; i < 10; i++) {
            String title = generateRandomTitle();
            String description = "学习任务：" + title;
            String planType = PLAN_TYPES[random.nextInt(PLAN_TYPES.length)];
            String subject = SUBJECTS[random.nextInt(SUBJECTS.length)];
            String difficulty = DIFFICULTY_LEVELS[random.nextInt(DIFFICULTY_LEVELS.length)];
            String status = STATUSES[random.nextInt(STATUSES.length)];
            int progressPercent = status.equals("已完成") ? 100 : random.nextInt(90) + 1; // 1-90%
            LocalDate startDate = today.minusDays(random.nextInt(7) + 1); // 1-7天前开始
            LocalDate endDate = today;

            insertStudyPlan(id++, 1, title, description, planType, subject, difficulty, status, progressPercent,
                    startDate, endDate);
        }

        // 生成10条其他日期结束的计划
        for (int i = 0; i < 10; i++) {
            String title = generateRandomTitle();
            String description = "学习任务：" + title;
            String planType = PLAN_TYPES[random.nextInt(PLAN_TYPES.length)];
            String subject = SUBJECTS[random.nextInt(SUBJECTS.length)];
            String difficulty = DIFFICULTY_LEVELS[random.nextInt(DIFFICULTY_LEVELS.length)];
            String status = STATUSES[random.nextInt(STATUSES.length)];
            int progressPercent = status.equals("已完成") ? 100 : random.nextInt(90) + 1; // 1-90%

            // 随机生成结束日期（不是今天）
            LocalDate endDate;
            if (random.nextBoolean()) {
                // 过去的日期
                endDate = today.minusDays(random.nextInt(30) + 1); // 1-30天前
            } else {
                // 未来的日期
                endDate = today.plusDays(random.nextInt(30) + 1); // 1-30天后
            }

            LocalDate startDate = endDate.minusDays(random.nextInt(7) + 1); // 1-7天前开始

            insertStudyPlan(id++, 1, title, description, planType, subject, difficulty, status, progressPercent,
                    startDate, endDate);
        }

        System.out.println("学习计划测试数据生成完成！共生成20条数据。");
    }

    /**
     * 生成并插入额外的测试数据，不覆盖现有数据
     * 
     * @param count 要生成的记录数量
     */
    public void generateAdditionalTestData(int count) {
        System.out.println("开始生成额外的学习计划测试数据...");

        // 获取当前最大ID
        int startId = getCurrentMaxId() + 1;
        LocalDate today = LocalDate.now();
        int id = startId;

        // 生成指定数量的记录
        for (int i = 0; i < count; i++) {
            String title = generateRandomTitle();
            String description = "学习任务：" + title;
            String planType = PLAN_TYPES[random.nextInt(PLAN_TYPES.length)];
            String subject = SUBJECTS[random.nextInt(SUBJECTS.length)];
            String difficulty = DIFFICULTY_LEVELS[random.nextInt(DIFFICULTY_LEVELS.length)];
            String status = STATUSES[random.nextInt(STATUSES.length)];
            int progressPercent = status.equals("已完成") ? 100 : random.nextInt(90) + 1; // 1-90%

            // 生成结束日期，分布在过去一周、过去一个月和未来
            LocalDate endDate = generateEndDate(today, i % 3);
            LocalDate startDate = endDate.minusDays(random.nextInt(7) + 1); // 1-7天前开始

            insertStudyPlan(id++, 1, title, description, planType, subject, difficulty, status, progressPercent,
                    startDate, endDate);
        }

        System.out.println("额外学习计划测试数据生成完成！共生成" + count + "条数据。");
        System.out.println("当前总数据量：" + (getCurrentMaxId()) + "条。");
    }

    /**
     * 获取当前最大ID
     */
    private int getCurrentMaxId() {
        try {
            Integer maxId = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(MAX(id), 0) FROM public.study_plans", Integer.class);
            return maxId != null ? maxId : 0;
        } catch (Exception e) {
            System.out.println("获取最大ID时发生异常: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 生成结束日期
     * 
     * @param today 当前日期
     * @param type  类型：0=过去一周，1=过去一个月，2=未来
     */
    private LocalDate generateEndDate(LocalDate today, int type) {
        switch (type) {
            case 0: // 过去一周
                return today.minusDays(random.nextInt(7) + 1); // 1-7天前
            case 1: // 过去一个月
                return today.minusDays(random.nextInt(23) + 8); // 8-30天前
            case 2: // 未来
                return today.plusDays(random.nextInt(30) + 1); // 1-30天后
            default:
                return today;
        }
    }

    /**
     * 生成随机标题
     */
    private String generateRandomTitle() {
        String prefix = TITLE_PREFIXES[random.nextInt(TITLE_PREFIXES.length)];
        String suffix = TITLE_SUFFIXES[random.nextInt(TITLE_SUFFIXES.length)];
        return prefix + suffix;
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
     * 插入单条学习计划数据
     */
    private void insertStudyPlan(
            int id, int userId, String title, String description,
            String planType, String subject, String difficulty,
            String status, int progressPercent, LocalDate startDate, LocalDate endDate) {

        try {
            String sql = "INSERT INTO public.study_plans (id, user_id, title, description, plan_type, subject, difficulty, status, progress_percent, start_date, end_date, created_at, updated_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

            jdbcTemplate.update(sql,
                    id, userId, title, description, planType, subject, difficulty, status,
                    progressPercent, startDate, endDate);

        } catch (Exception e) {
            System.out.println("插入数据时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 主方法，可直接运行
     */
    public static void main(String[] args) {
        // 注意：此方法需要在Spring容器中运行
        // 或者可以修改为使用Spring Boot的测试环境
        System.out.println("请在Spring容器中运行此生成器，或使用以下SQL语句手动插入数据：");

        // 生成SQL语句示例
        LocalDate today = LocalDate.now();
        Random random = new Random();

        System.out.println("-- 清空原有数据");
        System.out.println("DELETE FROM public.study_plans;");
        System.out.println();

        System.out.println("-- 插入测试数据");
        for (int i = 1; i <= 20; i++) {
            String title = TITLE_PREFIXES[random.nextInt(TITLE_PREFIXES.length)] + 
                          TITLE_SUFFIXES[random.nextInt(TITLE_SUFFIXES.length)];
            String description = "学习任务：" + title;
            String planType = PLAN_TYPES[random.nextInt(PLAN_TYPES.length)];
            String subject = SUBJECTS[random.nextInt(SUBJECTS.length)];
            String difficulty = DIFFICULTY_LEVELS[random.nextInt(DIFFICULTY_LEVELS.length)];
            String status = STATUSES[random.nextInt(STATUSES.length)];
            int progressPercent = status.equals("已完成") ? 100 : random.nextInt(90) + 1;

            LocalDate endDate;
            if (i <= 10) {
                // 前10条使用今天的日期
                endDate = today;
            } else {
                // 后10条使用随机日期
                if (random.nextBoolean()) {
                    endDate = today.minusDays(random.nextInt(30) + 1);
                } else {
                    endDate = today.plusDays(random.nextInt(30) + 1);
                }
            }

            LocalDate startDate = endDate.minusDays(random.nextInt(7) + 1);

            String sql = String.format(
                "INSERT INTO public.study_plans (id, user_id, title, description, plan_type, subject, difficulty, status, progress_percent, start_date, end_date, created_at, updated_at) " +
                "VALUES (%d, 1, '%s', '%s', '%s', '%s', '%s', '%s', %d, '%s', '%s', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);",
                i, title, description, planType, subject, difficulty, status, progressPercent, startDate, endDate
            );
            System.out.println(sql);
        }
    }
}
