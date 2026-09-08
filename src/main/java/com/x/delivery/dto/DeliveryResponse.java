package com.x.delivery.dto;
import com.x.delivery.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DeliveryResponse(Long id, Long orderId, Long storeId, DeliveryProviderType providerType,
                               DeliveryStatus status, String trackingNumber, String riderName, String riderPhone,
                               BigDecimal deliveryFee, BigDecimal courierCost, BigDecimal codAmount,
                               BigDecimal remittedAmount, LocalDateTime remittedAt, LocalDateTime createdAt) {
    public DeliveryResponse(Long id, Long orderId, Long storeId, DeliveryProviderType providerType,
                            DeliveryStatus status, String trackingNumber, String riderName, String riderPhone,
                            BigDecimal deliveryFee, BigDecimal courierCost, BigDecimal codAmount,
                            BigDecimal remittedAmount) {
        this(id, orderId, storeId, providerType, status, trackingNumber, riderName, riderPhone,
             deliveryFee, courierCost, codAmount, remittedAmount, null, null);
    }
}
