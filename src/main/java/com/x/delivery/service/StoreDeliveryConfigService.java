package com.x.delivery.service;

import com.x.delivery.dto.StoreDeliveryConfigResponse;
import com.x.delivery.dto.UpdateStoreDeliveryConfigRequest;
import com.x.delivery.entity.DeliveryProviderType;
import com.x.delivery.entity.StoreDeliveryConfig;
import com.x.delivery.repository.StoreDeliveryConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StoreDeliveryConfigService {
    private static final Set<DeliveryProviderType> DEFAULT_PROVIDERS =
            Set.copyOf(EnumSet.allOf(DeliveryProviderType.class));
    private static final DeliveryProviderType DEFAULT_PROVIDER = DeliveryProviderType.GRAB;

    private final StoreDeliveryConfigRepository configRepository;
    private final StoreLocationClient storeLocationClient;

    @Transactional(readOnly = true)
    public StoreDeliveryConfigResponse get(Long storeId) {
        StoreLocationClient.StoreDetails store = storeLocationClient.getStoreDetails(storeId);
        return configRepository.findByStoreId(storeId)
                .map(StoreDeliveryConfigResponse::from)
                .orElseGet(() -> new StoreDeliveryConfigResponse(storeId, store.businessId(), DEFAULT_PROVIDERS,
                        DEFAULT_PROVIDER, "SIMULATED", null));
    }

    @Transactional
    public StoreDeliveryConfigResponse update(Long storeId, UpdateStoreDeliveryConfigRequest request) {
        if (!request.enabledProviders().contains(request.defaultProvider())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The default provider must be enabled for this store");
        }
        StoreLocationClient.StoreDetails store = storeLocationClient.getStoreDetails(storeId);
        StoreDeliveryConfig config = configRepository.findByStoreId(storeId)
                .orElseGet(() -> StoreDeliveryConfig.builder().storeId(storeId).businessId(store.businessId()).build());
        config.setBusinessId(store.businessId());
        config.setEnabledProviders(new LinkedHashSet<>(request.enabledProviders()));
        config.setDefaultProvider(request.defaultProvider());
        return StoreDeliveryConfigResponse.from(configRepository.save(config));
    }

    @Transactional(readOnly = true)
    public Set<DeliveryProviderType> enabledProviders(Long storeId) {
        return configRepository.findByStoreId(storeId)
                .map(config -> Set.copyOf(config.getEnabledProviders()))
                .orElse(DEFAULT_PROVIDERS);
    }

    @Transactional(readOnly = true)
    public DeliveryProviderType defaultProvider(Long storeId) {
        return configRepository.findByStoreId(storeId)
                .map(StoreDeliveryConfig::getDefaultProvider)
                .orElse(DEFAULT_PROVIDER);
    }

    @Transactional(readOnly = true)
    public boolean isEnabled(Long storeId, DeliveryProviderType provider) {
        return enabledProviders(storeId).contains(provider);
    }
}
