package com.example.demo.repository;

import com.example.demo.model.Payment;
import com.example.demo.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Calculate total payments received by a vendor
    @Query("SELECT SUM(p.amount) FROM Payment p JOIN Order o ON p.orderId = o.orderId WHERE o.userId = :userId AND p.status = 'COMPLETED'")
    BigDecimal calculateTotalPaymentsForVendor(@Param("userId") Integer userId);

    // Find payments by status and order IDs
    List<Payment> findByStatusAndOrderIdIn(PaymentStatus status, List<Long> orderIds);
}
