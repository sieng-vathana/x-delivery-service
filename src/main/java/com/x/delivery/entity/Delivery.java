package com.x.delivery.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "deliveries") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Delivery {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    @Column(name = "order_id", nullable = false, unique = true) private Long orderId;
    @Column(name = "store_id", nullable = false) private Long storeId;
    @Column(name = "quote_id", nullable = false, unique = true) private Long quoteId;
    @Column(name = "idempotency_key", unique = true, length = 128) private String idempotencyKey;
    @Enumerated(EnumType.STRING) @Column(name = "provider_type", nullable = false, length = 32) private DeliveryProviderType providerType;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private DeliveryStatus status;
    @Column(name = "tracking_number", unique = true, length = 80) private String trackingNumber;
    @Column(name = "rider_name", length = 120) private String riderName;
    @Column(name = "rider_phone", length = 32) private String riderPhone;
    @Column(name = "delivery_fee", precision = 12, scale = 2) private BigDecimal deliveryFee;
    @Column(name = "courier_cost", precision = 12, scale = 2) private BigDecimal courierCost;
    @Column(name = "cod_amount", precision = 12, scale = 2) private BigDecimal codAmount;
    @Column(name = "remitted_amount", precision = 12, scale = 2) private BigDecimal remittedAmount;
    @Column(name = "remitted_at") private LocalDateTime remittedAt;
    @Column(name = "failure_reason", length = 500) private String failureReason;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
