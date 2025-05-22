package com.example.demo.repository;

import com.example.demo.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find payments for an order
    List<Payment> findByOrderId(Long orderId);

    // Calculate total payments received by a vendor
    @Query("SELECT SUM(p.amount) FROM Payment p JOIN Order o ON p.orderId = o.orderId WHERE o.vendorId = :vendorId AND p.status = 'COMPLETED'")
    BigDecimal calculateTotalPaymentsForVendor(@Param("vendorId") Integer vendorId);

    // Find payments by status and order IDs
    List<Payment> findByStatusAndOrderIdIn(com.example.demo.model.PaymentStatus status, List<Long> orderIds);
}
