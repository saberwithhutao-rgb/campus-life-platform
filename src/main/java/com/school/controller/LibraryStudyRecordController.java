package com.school.controller;

import com.school.entity.ResultVO;
import com.school.service.LibraryStudyRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 图书馆学习时长记录控制器
 */
@RestController
@RequestMapping("/api/library")
public class LibraryStudyRecordController {

    @Autowired
    private LibraryStudyRecordService libraryStudyRecordService;

    /**
     * 进入图书馆接口
     * 
     * @param userId 用户ID
     * @return 插入记录的ID
     */
    @PostMapping("/enter")
    public ResultVO enterLibrary(@RequestParam("userId") Long userId) {
        // 非空校验
        if (userId == null) {
            return ResultVO.fail("用户ID不能为空");
        }

        try {
            // 调用服务层进入图书馆
            Long recordId = libraryStudyRecordService.enterLibrary(userId);

            // 返回成功结果
            return ResultVO.success("进入图书馆成功", recordId);
        } catch (Exception e) {
            // 捕获异常
            e.printStackTrace();
            return ResultVO.fail("进入图书馆失败：" + e.getMessage());
        }
    }

    /**
     * 离开图书馆接口
     * 
     * @param recordId 记录ID
     * @return 本次学习时长（分钟）
     */
    @PostMapping("/leave")
    public ResultVO leaveLibrary(
            @RequestParam(value = "recordId", required = false) Long recordId,
            @RequestBody(required = false) Map<String, Object> requestBody) {

        // 1. 从请求体中获取 recordId（如果 URL 参数中没有）
        if (recordId == null && requestBody != null) {
            Object recordIdObj = requestBody.get("recordId");
            if (recordIdObj != null) {
                try {
                    recordId = Long.parseLong(recordIdObj.toString());
                } catch (NumberFormatException e) {
                    return ResultVO.fail("recordId 参数格式错误");
                }
            }
        }

        // 2. 非空校验
        if (recordId == null) {
            return ResultVO.fail("记录ID不能为空");
        }

        try {
            // 3. 调用服务层离开图书馆
            Integer durationMinutes = libraryStudyRecordService.leaveLibrary(recordId);

            if (durationMinutes >= 0) {
                // 时长大于等于0表示成功，时长为0表示用户进入后立即离开
                return ResultVO.success("离开图书馆成功", durationMinutes);
            } else {
                // 时长小于0表示记录不存在或已离开
                return ResultVO.fail("离开图书馆失败：记录不存在或已离开");
            }
        } catch (Exception e) {
            // 4. 捕获异常
            e.printStackTrace();
            return ResultVO.fail("离开图书馆失败：" + e.getMessage());
        }
    }

    /**
     * 查询今日总学习时长接口
     * 
     * @param userId 用户ID
     * @return 今日总学习时长（分钟）
     */
    @GetMapping("/today-duration")
    public ResultVO getTodayTotalDuration(@RequestParam("userId") Long userId) {
        // 非空校验
        if (userId == null) {
            return ResultVO.fail("用户ID不能为空");
        }

        try {
            // 调用服务层查询今日总学习时长
            Integer totalDuration = libraryStudyRecordService.getTodayTotalDuration(userId);

            // 返回成功结果
            return ResultVO.success("查询今日总学习时长成功", totalDuration);
        } catch (Exception e) {
            // 捕获异常
            e.printStackTrace();
            return ResultVO.fail("查询今日总学习时长失败：" + e.getMessage());
        }
    }
}