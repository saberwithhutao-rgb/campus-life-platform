package com.campus.forum.library.dto;

import lombok.Data;

@Data
public class AvailableSeatsDTO {
    private int totalSeats;
    private int usedSeats;
    private int availableSeats;

    public AvailableSeatsDTO(int totalSeats, int usedSeats) {
        this.totalSeats = totalSeats;
        this.usedSeats = usedSeats;
        this.availableSeats = totalSeats - usedSeats;
    }
}