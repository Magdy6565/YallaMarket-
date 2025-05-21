package com.example.demo.repository;

import com.example.demo.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    @Query("SELECT r FROM Refund r " +
            "JOIN r.orderItem oi " +
            "JOIN Product p ON p.productId = oi.productId " +
            "WHERE p.vendorId = :vendorId AND r.deletedAt IS NULL")
    List<Refund> findRefundsByVendorId(@Param("vendorId") Long vendorId);
}
