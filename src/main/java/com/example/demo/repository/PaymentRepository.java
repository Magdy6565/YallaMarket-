package com.example.demo.repository;

import com.example.demo.model.Payment;
import com.example.demo.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    // Calculate total payments received by a vendor
    @Query("SELECT SUM(p.amount) FROM Payment p JOIN Order o ON p.orderId = o.orderId WHERE o.userId = :userId AND p.status = 'COMPLETED'")
    BigDecimal calculateTotalPaymentsForVendor(@Param("userId") Integer userId);

    // Find payments by status and order IDs
    List<Payment> findByStatusAndOrderIdIn(PaymentStatus status, List<Long> orderIds);
    
    // Find payments where deleted_at is null
    @Query("SELECT p FROM Payment p WHERE p.deletedAt IS NULL")
    Page<Payment> findAllActive(Pageable pageable);
      // Find payment by ID and not deleted
    @Query("SELECT p FROM Payment p WHERE p.paymentId = :paymentId AND p.deletedAt IS NULL")
    Optional<Payment> findByIdAndNotDeleted(@Param("paymentId") Long paymentId);
}
