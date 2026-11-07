package com.x.delivery.dto;
import com.x.delivery.entity.DeliveryProviderType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record DeliveryQuoteResponse(Long id, Long storeId, DeliveryProviderType providerType,
        BigDecimal distanceKm, BigDecimal quotedFee, String currencyCode, LocalDateTime expiresAt,
        BigDecimal destinationLatitude, BigDecimal destinationLongitude) { }
