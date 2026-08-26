package com.example.booking.repository;

import com.example.booking.entity.Reservation;
import com.example.booking.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
                select r from Reservation r
                where (:userId is null or r.user.id = :userId)
                  and (:status is null or r.status = :status)
                  and (:minPrice is null or r.price >= :minPrice)
                  and (:maxPrice is null or r.price <= :maxPrice)
            """)
    Page<Reservation> search(
            @Param("userId") Long userId,
            @Param("status") ReservationStatus status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
}
