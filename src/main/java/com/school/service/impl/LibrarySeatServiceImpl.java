package com.school.service.impl;

import com.school.entity.LibrarySeatGlobal;
import com.school.entity.LibrarySeatOperation;
import com.school.repository.LibrarySeatGlobalRepository;
import com.school.repository.LibrarySeatOperationRepository;
import com.school.service.LibrarySeatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LibrarySeatServiceImpl implements LibrarySeatService {

    private final LibrarySeatGlobalRepository seatGlobalRepository;
    private final LibrarySeatOperationRepository seatOperationRepository;

    public LibrarySeatServiceImpl(LibrarySeatGlobalRepository seatGlobalRepository, 
                               LibrarySeatOperationRepository seatOperationRepository) {
        this.seatGlobalRepository = seatGlobalRepository;
        this.seatOperationRepository = seatOperationRepository;
    }

    @Override
    @Transactional
    public LibrarySeatGlobal enterSeat() {
        LibrarySeatGlobal seatGlobal = seatGlobalRepository.findFirst();
        if (seatGlobal == null) {
            throw new RuntimeException("座位数据不存在");
        }

        LibrarySeatGlobal updatedSeat = seatGlobalRepository.incrementOccupiedSeats(seatGlobal.getId());
        if (updatedSeat == null) {
            throw new RuntimeException("座位已满，无法进入");
        }

        LibrarySeatOperation operation = new LibrarySeatOperation();
        operation.setUserId(1); // 固定用户ID为1
        operation.setOperationType(1); // 1=进入
        operation.setOperationTime(LocalDateTime.now());
        seatOperationRepository.save(operation);

        return updatedSeat;
    }

    @Override
    @Transactional
    public LibrarySeatGlobal leaveSeat() {
        LibrarySeatGlobal seatGlobal = seatGlobalRepository.findFirst();
        if (seatGlobal == null) {
            throw new RuntimeException("座位数据不存在");
        }

        LibrarySeatGlobal updatedSeat = seatGlobalRepository.decrementOccupiedSeats(seatGlobal.getId());
        if (updatedSeat == null) {
            throw new RuntimeException("无占用座位，无法离开");
        }

        LibrarySeatOperation operation = new LibrarySeatOperation();
        operation.setUserId(1); // 固定用户ID为1
        operation.setOperationType(2); // 2=离开
        operation.setOperationTime(LocalDateTime.now());
        seatOperationRepository.save(operation);

        return updatedSeat;
    }

    @Override
    public LibrarySeatGlobal getSeatStatus() {
        LibrarySeatGlobal seatGlobal = seatGlobalRepository.findFirst();
        if (seatGlobal == null) {
            throw new RuntimeException("座位数据不存在");
        }
        return seatGlobal;
    }
}