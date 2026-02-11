package com.school.service;

import com.school.entity.LibrarySeatGlobal;

public interface LibrarySeatService {
    LibrarySeatGlobal enterSeat();
    LibrarySeatGlobal leaveSeat();
    LibrarySeatGlobal getSeatStatus();
}