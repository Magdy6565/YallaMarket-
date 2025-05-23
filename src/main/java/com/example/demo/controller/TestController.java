package com.example.demo.controller;

import com.example.demo.dto.PaymentDTO;
import com.example.demo.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
@RequestMapping("/test")
public class TestController {

    private final PaymentService paymentService;

    @Autowired
    public TestController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }    @GetMapping("/payment/{id}")
    @ResponseBody
    public String testPayment(@PathVariable Long id) {
        Optional<PaymentDTO> payment = paymentService.getPaymentById(id);
        if (payment.isPresent()) {
            PaymentDTO p = payment.get();
            return String.format("Payment found: ID=%d, Amount=$%.2f, Method=%s, Status=%s",
                    p.getPaymentId(), p.getAmount(), p.getPaymentMethod(), p.getStatus());
        } else {
            return "Payment not found";
        }
    }
}
