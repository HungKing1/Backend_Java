package com.service;

import com.entity.*;
import com.entity.dto.OrderItemDTO;
import com.enums.OrderStatus;
import com.repository.*;
import com.request.Item;
import com.request.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Observer Pattern: Event chứa thông tin order notification
 */
class OrderNotificationEvent {
    private final Order order;
    private final User user;
    private final String recipientEmail;
    private final String recipientName;
    private final String subject;
    private final String content;

    public OrderNotificationEvent(Order order, User user, String recipientEmail, String recipientName,
            String subject, String content) {
        this.order = order;
        this.user = user;
        this.recipientEmail = recipientEmail;
        this.recipientName = recipientName;
        this.subject = subject;
        this.content = content;
    }

    public Order getOrder() {
        return order;
    }

    public User getUser() {
        return user;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }
}

/**
 * Observer Pattern: Interface định nghĩa Observer
 */
interface OrderNotificationObserver {
    /**
     * Xử lý order notification event
     */
    void handleOrderNotification(OrderNotificationEvent event);
}

/**
 * Observer Pattern: Observer gửi email cho user
 */
class UserEmailObserver implements OrderNotificationObserver {
    private final EmailService emailService;

    public UserEmailObserver(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void handleOrderNotification(OrderNotificationEvent event) {
        if (event.getRecipientEmail() == null || event.getRecipientEmail().isEmpty()) {
            System.out.println("[UserEmailObserver] No email address, skipping");
            return;
        }

        try {
            emailService.sendEmail(event.getRecipientEmail(), event.getSubject(), event.getContent());
            System.out.println("[UserEmailObserver] Email sent to user: " + event.getRecipientEmail());
        } catch (Exception e) {
            System.err.println("[UserEmailObserver] Failed to send email: " + e.getMessage());
        }
    }
}

/**
 * Observer Pattern: Subject quản lý observers và publish events
 */
class OrderNotificationService {
    private final List<OrderNotificationObserver> observers = new ArrayList<>();
    private final EmailService emailService;
    private boolean initialized = false;

    public OrderNotificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Khởi tạo observers (lazy initialization)
     */
    private void initializeObservers() {
        if (initialized) {
            return;
        }
        observers.add(new UserEmailObserver(emailService));
        initialized = true;
        System.out.println("[OrderNotificationService] Observers initialized: " + observers.size());
    }

    /**
     * Đăng ký observer mới
     */
    public void registerObserver(OrderNotificationObserver observer) {
        observers.add(observer);
        System.out.println("[OrderNotificationService] Observer registered: " + observer.getClass().getSimpleName());
    }

    /**
     * Publish event và notify tất cả observers
     */
    public void notifyObservers(OrderNotificationEvent event) {
        initializeObservers();
        System.out.println("[OrderNotificationService] Publishing order notification for order #"
                + event.getOrder().getOrderId());

        for (OrderNotificationObserver observer : observers) {
            try {
                observer.handleOrderNotification(event);
            } catch (Exception e) {
                System.err.println("[OrderNotificationService] Error in observer "
                        + observer.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }
}

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private CartService cartService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;

    // Observer Pattern: Notification service để quản lý observers
    private OrderNotificationService orderNotificationService;

    /**
     * Khởi tạo OrderNotificationService (lazy initialization)
     */
    private OrderNotificationService getOrderNotificationService() {
        if (orderNotificationService == null) {
            orderNotificationService = new OrderNotificationService(emailService);
        }
        return orderNotificationService;
    }

    public Integer createOrder(OrderRequest orderRequest, String token) {
        if (orderRequest.getItems().length == 0)
            return -1;

        // Handle both authenticated and guest users
        User user = null;
        Integer userId = null;

        if (token != null && !token.isEmpty() && !token.equals("null")) {
            Optional<User> optionalUser = userService.getInfo(token);
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
                userId = user.getUserId();
            }
        }

        Order order = new Order(orderRequest, userId);
        Double subtotal = 0.0;
        for (Item item : orderRequest.getItems()) {
            Product product = productRepository.findById(item.getProductId()).get();
            subtotal += product.getCurrentPrice() * item.getQuantity();
        }

        Invoice invoice = invoiceRepository.findById(orderRequest.getInvoiceId()).get();
        order.setInvoice(invoice);
        invoice.setOrder(order);

        Transaction transaction = transactionRepository.findById(orderRequest.getTransactionId()).get();
        transaction.setOrder(order);

        List<Transaction> transactions = order.getTransactions() != null
                ? new ArrayList<>(order.getTransactions())
                : new ArrayList<>();

        transactions.add(transaction);
        order.setTransactions(transactions);

        order.setSubtotal(subtotal);
        order.setShippingFee((double) orderRequest.getShippingFee());
        order.setTotalAmount(subtotal + (double) orderRequest.getShippingFee());
        String name = orderRequest.getReceiverName();
        if (name == null || name.isEmpty())
            name = "guest";
        order.setReceiverName(name);
        orderRepository.save(order);

        for (Item item : orderRequest.getItems()) {
            Product product = productRepository.findById(item.getProductId()).get();
            OrderItem orderItem = new OrderItem(order.getOrderId(), item.getProductId(),
                    item.getQuantity(), product.getCurrentPrice());
            orderItemRepository.save(orderItem);

            // Only remove from cart if user is authenticated
            if (userId != null) {
                cartService.removeCartItemWhenCreateOrder(userId, item.getProductId());
            }

            // Auto-update product stock (SRS requirement)
            if (product.getStockQuantity() != null) {
                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                productRepository.save(product);
            }
        }

        // --- Observer Pattern: Gửi email xác nhận đơn hàng cho user ---
        try {
            String to = (user != null) ? user.getEmail() : orderRequest.getReceiverEmail();
            String subject = "Xác nhận đơn hàng #" + order.getOrderId();
            StringBuilder content = new StringBuilder();
            String receiverName = (user != null) ? user.getFullName() : orderRequest.getReceiverName();
            content.append("Chào ").append(receiverName).append(",\n\n");
            content.append("Cảm ơn bạn đã đặt hàng tại cửa hàng của chúng tôi.\n");
            content.append("Thông tin đơn hàng:\n");
            content.append("Mã đơn hàng: ").append(order.getOrderId()).append("\n");
            content.append("Tổng tiền: ").append(order.getTotalAmount()).append(" VND\n");
            content.append("Phí vận chuyển: ").append(order.getShippingFee()).append(" VND\n");
            content.append("Ghi chú: ").append(order.getNote() != null ? order.getNote() : "Không có").append("\n\n");
            content.append("Chúng tôi sẽ sớm xử lý đơn hàng của bạn.\n");
            content.append("Trân trọng,\nĐội ngũ cửa hàng");

            // Observer Pattern: Tạo event và notify observers
            OrderNotificationEvent event = new OrderNotificationEvent(order, user, to, receiverName, subject,
                    content.toString());
            getOrderNotificationService().notifyObservers(event);
        } catch (Exception e) {
            System.out.println("Gửi email thất bại: " + e.getMessage());
        }

        return order.getOrderId();
    }

    public Optional<Order> getOrderById(Integer orderId) {
        return orderRepository.findOrderByOrderId(orderId);
    }

    public List<Order> getOrderHistory(Integer userId) {
        return orderRepository.findOrdersByUserId(userId);
    }

    public List<Order> getOrderByStatus(OrderStatus status) {
        return orderRepository.findOrdersByStatus(status);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order applyOrderStatus(Integer orderId, OrderStatus status) {
        Optional<Order> order = orderRepository.findOrderByOrderId(orderId);
        if (order.isPresent()) {
            if (order.get().getSubtotal() == null) {
                order.get().setSubtotal(0.0);
            }
            order.get().setStatus(status);
            orderRepository.save(order.get());
            if (status == OrderStatus.PROCESSING) {
                // PM starts processing the order
            }
            return order.get();
        } else {
            return null;
        }
    }

    public List<OrderItemDTO> getProductsByOrderId(Integer orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        List<OrderItemDTO> orderItemDTOs = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            OrderItemDTO item = new OrderItemDTO();
            Optional<Product> optionalProduct = productRepository.findByProductId(orderItem.getProductId());
            if (optionalProduct.isEmpty()) {
                item.setProductId(orderItem.getProductId());
            } else {
                Product product = optionalProduct.get();
                item.setProductId(product.getProductId());
                item.setTitle(product.getTitle());
                item.setDescription(product.getDescription());
                item.setWeight(product.getWeight());
                item.setQuantity(orderItem.getQuantity());
                item.setPrice(orderItem.getPrice());
                item.setImageUrl(product.getImageUrl());
            }
            orderItemDTOs.add(item);
        }
        return orderItemDTOs;
    }
}
