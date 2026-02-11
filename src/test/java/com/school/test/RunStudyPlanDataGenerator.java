package com.school.test;

import com.school.test.StudyPlanTestDataGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 运行学习计划测试数据生成器
 */
@SpringBootTest
public class RunStudyPlanDataGenerator {

    @Autowired
    private StudyPlanTestDataGenerator studyPlanTestDataGenerator;

    @Test
    public void generateTestData() {
        studyPlanTestDataGenerator.generateAndInsertTestData();
    }
}
