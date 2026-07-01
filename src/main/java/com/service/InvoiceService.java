package com.service;

import com.entity.Invoice;
import com.entity.InvoiceItem;
import com.entity.Product;
import com.repository.*;
import com.request.InvoiceItemRequest;
import com.request.InvoiceRequest;
import com.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InvoiceItemRepository invoiceItemRepository;

    public ResponseEntity<ApiResponse> createInvoice(InvoiceRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Item list is empty", null));
        }

        double subtotal = 0;
        List<InvoiceItem> invoiceItems = new ArrayList<>();

        Invoice invoice = new Invoice();
        invoice.setStatus(Invoice.InvoiceStatus.PENDING);
        invoice.setCreatedAt(LocalDateTime.now());

        for (InvoiceItemRequest itemReq : request.getItems()) {

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException(
                            "Product not found with id: " + itemReq.getProductId()));

            if (product.getStockQuantity() < itemReq.getQuantity()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false,
                                "Not enough stock for product: " + product.getTitle(),
                                null));
            }

            double unitPrice = product.getCurrentPrice();
            int quantity = itemReq.getQuantity();

            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setTotalPrice(unitPrice * quantity);

            subtotal += unitPrice * quantity;
            invoiceItems.add(item);
        }

        double vat = subtotal * 0.10;
        double totalWithVAT = subtotal + vat;

        double deliveryFee = request.getShippingFee();

        double totalAmount = totalWithVAT + deliveryFee;

        invoice.setSubtotal(subtotal);
        invoice.setTotalWithVAT(totalWithVAT);
        invoice.setDeliveryFee(deliveryFee);
        invoice.setTotalAmount(totalAmount);
        invoice.setInvoiceItems(invoiceItems);
        invoice.setDeliveryFee(deliveryFee);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        return ResponseEntity.ok(
                new ApiResponse(true, "Invoice created successfully", savedInvoice));
    }
}
