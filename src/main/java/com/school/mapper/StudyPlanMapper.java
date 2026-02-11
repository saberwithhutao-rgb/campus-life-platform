package com.school.mapper;

import com.school.entity.StudyPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 学习计划Mapper接口
 */
@Mapper
public interface StudyPlanMapper {

  /**
   * 批量插入学习计划
   * 
   * @param studyPlans 学习计划列表
   */
  void insertBatch(List<StudyPlan> studyPlans);

  /**
   * 清空所有学习计划数据
   */
  void deleteAll();

  /**
   * 根据用户ID和时间范围获取学习计划列表
   * 
   * @param userId    用户ID
   * @param startTime 开始时间
   * @param endTime   结束时间
   * @return 学习计划列表
   */
  List<StudyPlan> getStudyPlansByUserIdAndTimeRange(
      @Param("userId") Integer userId,
      @Param("startTime") String startTime,
      @Param("endTime") String endTime);

  /**
   * 获取用户学习计划总数
   * 
   * @param userId    用户ID
   * @param startTime 开始时间
   * @param endTime   结束时间
   * @return 计划总数
   */
  int getTotalPlanCount(
      @Param("userId") Integer userId,
      @Param("startTime") String startTime,
      @Param("endTime") String endTime);

  /**
   * 获取用户已完成学习计划数
   * 
   * @param userId    用户ID
   * @param startTime 开始时间
   * @param endTime   结束时间
   * @return 已完成计划数
   */
  int getCompletedPlanCount(
      @Param("userId") Integer userId,
      @Param("startTime") String startTime,
      @Param("endTime") String endTime);

  /**
   * 获取用户学习计划平均进度
   * 
   * @param userId    用户ID
   * @param startTime 开始时间
   * @param endTime   结束时间
   * @return 平均进度
   */
  Double getAverageProgress(
      @Param("userId") Integer userId,
      @Param("startTime") String startTime,
      @Param("endTime") String endTime);

  /**
   * 获取用户延期学习计划数
   * 
   * @param userId      用户ID
   * @param currentDate 当前日期
   * @param startTime   开始时间
   * @param endTime     结束时间
   * @return 延期计划数
   */
  int getOverduePlanCount(
      @Param("userId") Integer userId,
      @Param("currentDate") String currentDate,
      @Param("startTime") String startTime,
      @Param("endTime") String endTime);

  /**
   * 获取用户学习计划难度分布
   * 
   * @param userId    用户ID
   * @param startTime 开始时间
   * @param endTime   结束时间
   * @return 难度分布Map，key为难度，value为数量
   */
  List<Map<String, Object>> getDifficultyDistribution(
      @Param("userId") Integer userId,
      @Param("startTime") String startTime,
      @Param("endTime") String endTime);

  /**
   * 获取用户学习计划类型分布
   * 
   * @param userId    用户ID
   * @param startTime 开始时间
   * @param endTime   结束时间
   * @return 类型分布Map，key为类型，value为数量
   */
  List<Map<String, Object>> getPlanTypeDistribution(
      @Param("userId") Integer userId,
      @Param("startTime") String startTime,
      @Param("endTime") String endTime);

  /**
   * 获取用户各科目学习计划数量
   * 
   * @param userId    用户ID
   * @param startTime 开始时间
   * @param endTime   结束时间
   * @return 科目分布Map，key为科目，value为数量
   */
  List<Map<String, Object>> getSubjectDistribution(
      @Param("userId") Integer userId,
      @Param("startTime") String startTime,
      @Param("endTime") String endTime);
}
