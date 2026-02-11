package com.school.controller;

import com.school.entity.LibrarySeatGlobal;
import com.school.entity.ResultVO;
import com.school.service.LibrarySeatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/library/seat")
public class LibrarySeatController {

    private final LibrarySeatService librarySeatService;

    public LibrarySeatController(LibrarySeatService librarySeatService) {
        this.librarySeatService = librarySeatService;
    }

    @PostMapping("/enter")
    public ResultVO enterSeat() {
        try {
            LibrarySeatGlobal seatStatus = librarySeatService.enterSeat();
            return ResultVO.success("进入座位成功", seatStatus);
        } catch (RuntimeException e) {
            return ResultVO.fail(e.getMessage());
        }
    }

    @PostMapping("/leave")
    public ResultVO leaveSeat() {
        try {
            LibrarySeatGlobal seatStatus = librarySeatService.leaveSeat();
            return ResultVO.success("离开座位成功", seatStatus);
        } catch (RuntimeException e) {
            return ResultVO.fail(e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResultVO getSeatStatus() {
        try {
            LibrarySeatGlobal seatStatus = librarySeatService.getSeatStatus();
            return ResultVO.success("success", seatStatus);
        } catch (RuntimeException e) {
            return ResultVO.fail(e.getMessage());
        }
    }
}