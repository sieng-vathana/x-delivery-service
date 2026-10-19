package com.x.delivery.dto;

import com.x.delivery.entity.DeliveryProviderType;
import com.x.delivery.entity.StoreDeliveryConfig;

import java.time.LocalDateTime;
import java.util.Set;

public record StoreDeliveryConfigResponse(
        Long storeId,
        Long businessId,
        Set<DeliveryProviderType> enabledProviders,
        DeliveryProviderType defaultProvider,
        String integrationMode,
        LocalDateTime updatedAt) {

    public static StoreDeliveryConfigResponse from(StoreDeliveryConfig config) {
        return new StoreDeliveryConfigResponse(config.getStoreId(), config.getBusinessId(),
                Set.copyOf(config.getEnabledProviders()), config.getDefaultProvider(), "SIMULATED", config.getUpdatedAt());
    }
}
