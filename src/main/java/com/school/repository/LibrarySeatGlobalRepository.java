package com.school.repository;

import com.school.entity.LibrarySeatGlobal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LibrarySeatGlobalRepository extends JpaRepository<LibrarySeatGlobal, Long> {
    @Query(value = "SELECT * FROM library_seat_global LIMIT 1", nativeQuery = true)
    LibrarySeatGlobal findFirst();
    
    @Query(value = "UPDATE library_seat_global SET occupied_seats = occupied_seats + 1 WHERE id = ?1 AND occupied_seats < total_seats RETURNING *", nativeQuery = true)
    LibrarySeatGlobal incrementOccupiedSeats(Long id);
    
    @Query(value = "UPDATE library_seat_global SET occupied_seats = occupied_seats - 1 WHERE id = ?1 AND occupied_seats > 0 RETURNING *", nativeQuery = true)
    LibrarySeatGlobal decrementOccupiedSeats(Long id);
}