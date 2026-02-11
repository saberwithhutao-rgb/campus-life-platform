package com.school.mapper;

import com.school.entity.LibraryStudyRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 图书馆学习时长记录Mapper接口
 */
@Mapper
public interface LibraryStudyRecordMapper {

    /**
     * 插入图书馆学习记录
     * 
     * @param record 学习记录
     */
    @Insert(value = "INSERT INTO library_study_records (user_id, enter_time, leave_time, duration_minutes, create_time, update_time) "
            +
            "VALUES (#{userId}, #{enterTime}, #{leaveTime}, #{durationMinutes}, #{createTime}, #{updateTime})"
            +
            "")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(LibraryStudyRecord record);

    /**
     * 根据ID查询记录
     * 
     * @param id 记录ID
     * @return 学习记录
     */
    @Select("SELECT * FROM library_study_records WHERE id = #{id}")
    LibraryStudyRecord selectById(@Param("id") Long id);

    /**
     * 更新图书馆学习记录
     * 
     * @param record 学习记录
     */
    @Update("UPDATE library_study_records SET leave_time = #{leaveTime}, duration_minutes = #{durationMinutes}, update_time = #{updateTime} "
            +
            "WHERE id = #{id}")
    void updateById(LibraryStudyRecord record);

    /**
     * 查询未离开的记录
     * 
     * @param id 记录ID
     * @return 学习记录
     */
    @Select("SELECT * FROM library_study_records WHERE id = #{id} AND leave_time IS NULL")
    LibraryStudyRecord selectUnfinishedById(@Param("id") Long id);

    /**
     * 查询今日总学习时长
     * 
     * @param userId     用户ID
     * @param todayStart 今日凌晨0点
     * @return 今日总学习时长（分钟）
     */
    @Select("SELECT COALESCE(SUM(duration_minutes), 0) FROM library_study_records " +
            "WHERE user_id = #{userId} AND enter_time >= #{todayStart}::timestamp AND leave_time IS NOT NULL")
    Integer getTodayTotalDuration(@Param("userId") Long userId, @Param("todayStart") String todayStart);
}