package com.school.test;

import com.school.test.StudyPlanTestDataGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 运行学习计划测试数据生成器，添加额外30条数据
 */
@SpringBootTest
public class RunAdditionalStudyPlanDataGenerator {

    @Autowired
    private StudyPlanTestDataGenerator studyPlanTestDataGenerator;

    @Test
    public void generateAdditionalTestData() {
        // 生成30条额外数据，不覆盖现有数据
        studyPlanTestDataGenerator.generateAdditionalTestData(30);
    }
}
