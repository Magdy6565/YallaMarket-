package com.example.demo.repository;

import com.example.demo.model.Invoice;
import com.example.demo.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Invoice findByOrder(Order order);
}
