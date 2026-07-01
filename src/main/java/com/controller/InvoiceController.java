package com.controller;

import com.entity.Invoice;
import com.request.InvoiceRequest;
import com.response.ApiResponse;
import com.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invoice")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createInvoice(@RequestBody InvoiceRequest invoice) {
        return invoiceService.createInvoice(invoice);
    }
}
