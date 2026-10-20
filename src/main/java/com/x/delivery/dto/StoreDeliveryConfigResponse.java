package com.x.delivery.dto;

import com.x.delivery.entity.DeliveryProviderType;
import com.x.delivery.entity.StoreDeliveryConfig;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record StoreDeliveryConfigResponse(
        Long storeId,
        Long businessId,
        List<DeliveryProviderType> enabledProviders,
        DeliveryProviderType defaultProvider,
        String integrationMode,
        LocalDateTime updatedAt) {

    public static StoreDeliveryConfigResponse from(StoreDeliveryConfig config) {
        return new StoreDeliveryConfigResponse(config.getStoreId(), config.getBusinessId(),
                new ArrayList<>(config.getEnabledProviders()), config.getDefaultProvider(), "SIMULATED", config.getUpdatedAt());
    }
}
