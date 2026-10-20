package com.x.delivery.dto;

import com.x.delivery.entity.DeliveryProviderType;

public record DeliveryProviderOptionResponse(
        DeliveryProviderType providerType,
        String displayName,
        boolean enabled,
        String integrationMode) {
}
