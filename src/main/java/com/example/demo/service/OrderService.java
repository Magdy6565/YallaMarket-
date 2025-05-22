package com.example.demo.service;

import com.example.demo.dto.OrderItemRequest;
import com.example.demo.dto.OrderRequest;
import com.example.demo.dto.VendorOrderDetailsDto;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository; // Needed for filtering logic

    private final InvoiceRepository invoiceRepository;

    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;


    @Autowired
    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, InvoiceRepository invoiceRepository, PaymentRepository paymentRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
    }

    /**
     * Retrieves all non-deleted orders that contain products owned by the specified vendor (user).
     * @param vendorUserId The ID of the user who is the vendor.
     * @return A list of VendorOrderDetailsDto representing the orders.
     */
    @Transactional(readOnly = true) // Read-only transaction for fetching data
    public List<VendorOrderDetailsDto> getVendorOrders(Long vendorUserId) {
        List<Order> orders = orderRepository.findOrdersByProductVendorId(vendorUserId);

        // Map Order entities to VendorOrderDetailsDto
        return orders.stream()
                .map(VendorOrderDetailsDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of a specific order, ensuring it contains products owned by the vendor.
     * @param orderId The ID of the order to update.
     * @param vendorUserId The ID of the user who is the vendor.
     * @param newStatus The new status for the order (e.g., "approved", "denied").
     * @return An Optional containing the updated Order if found and owned by the vendor, otherwise empty.
     */
    @Transactional
    public Optional<Order> updateOrderStatus(Long orderId, Long vendorUserId, String newStatus) {
        // Find the order and ensure it contains products from this vendor
        Optional<Order> orderOptional = orderRepository.findOrderByOrderIdAndProductVendorId(orderId, vendorUserId);

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            // TODO: Add validation here to ensure the newStatus is a valid transition from the current status
            order.setStatus(newStatus);
            return Optional.of(orderRepository.save(order));
        } else {
            return Optional.empty(); // Order not found or does not belong to this vendor
        }
    }

    public List<VendorOrderDetailsDto> getOrdersByStatus(OrderStatus status) {
        // Convert the OrderStatus enum to its String name (e.g., "PENDING")
        // and pass it to the new repository method.
        return orderRepository.findOrdersBySpecificStatus(status.name());
    }

    public List<VendorOrderDetailsDto> getAllStoreOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(VendorOrderDetailsDto::new)
                .collect(Collectors.toList());
    }

    public List<VendorOrderDetailsDto> getAllStoreOrdersInvoicesByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);

        return orders.stream()
                .map(order -> {
                    Invoice invoice = invoiceRepository.findByOrder(order);
                    return new VendorOrderDetailsDto(order, invoice);
                })
                .collect(Collectors.toList());
    }


    @Transactional
    public Order placeOrder(OrderRequest orderRequest, Long userId) {
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderDate(LocalDate.now());
        order.setStatus("PLACED");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        List<Product> products = new ArrayList<>();

        for (OrderItemRequest itemReq : orderRequest.getItems()) {
            Product product = productRepository.findByProductIdAndAvailableQuantity(itemReq.getProductId(), itemReq.getQuantity())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            product.setQuantity(product.getQuantity() - itemReq.getQuantity());
            products.add(product);

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductId(product.getProductId());
            item.setQuantity(itemReq.getQuantity());
            item.setPriceEach(product.getPrice());

            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            orderItems.add(item);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        for (OrderItem item: orderItems) {
            item.setOrderId(savedOrder.getOrderId());
            item.setOrder(savedOrder);
            orderItemRepository.save(item);
        }
        productRepository.saveAll(products);

        // Payment
        Payment payment = new Payment();
        payment.setOrderId(savedOrder.getOrderId());
        payment.setUserId(userId);
        payment.setAmount(totalAmount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod(orderRequest.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        // Invoice
        Invoice invoice = new Invoice();
        invoice.setOrder(savedOrder);
        invoice.setPayment(payment);
        invoice.setIssueDate(LocalDateTime.now());
        invoice.setDueDate(LocalDateTime.now().plusDays(7));
        invoice.setStatus("ISSUED");
        invoice.setTotal(totalAmount);
        invoiceRepository.save(invoice);

        return savedOrder;
    }
}