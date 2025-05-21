package com.example.demo.dto;

import com.example.demo.model.Invoice;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDTO {
    private Long invoiceId;
    private LocalDateTime issueDate;
    private LocalDateTime dueDate;
    private BigDecimal total;
    private String status;
    private String pdfLink;

    public InvoiceDTO(Invoice invoice) {
        this.invoiceId = invoice.getInvoiceId();
        this.issueDate = invoice.getIssueDate();
        this.dueDate = invoice.getDueDate();
        this.total = invoice.getTotal();
        this.status = invoice.getStatus();
        this.pdfLink = invoice.getPdfLink();
    }
}
