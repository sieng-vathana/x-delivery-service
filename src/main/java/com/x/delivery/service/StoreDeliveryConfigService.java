package com.x.delivery.service;

import com.x.delivery.cache.DeliveryCacheNames;
import com.x.delivery.dto.StoreDeliveryConfigResponse;
import com.x.delivery.dto.UpdateStoreDeliveryConfigRequest;
import com.x.delivery.entity.DeliveryProviderType;
import com.x.delivery.entity.StoreDeliveryConfig;
import com.x.delivery.repository.StoreDeliveryConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StoreDeliveryConfigService {
    private static final DeliveryProviderType DEFAULT_PROVIDER = DeliveryProviderType.GRAB;

    private final StoreDeliveryConfigRepository configRepository;
    private final StoreLocationClient storeLocationClient;
    private final DeliveryProviderCatalogService providerCatalog;

    @Cacheable(cacheNames = DeliveryCacheNames.STORE_CONFIG, key = "#storeId")
    @Transactional(readOnly = true)
    public StoreDeliveryConfigResponse get(Long storeId) {
        StoreLocationClient.StoreDetails store = storeLocationClient.getStoreDetails(storeId);
        return configRepository.findByStoreId(storeId)
                .map(this::toResponse)
                .orElseGet(() -> {
                    Set<DeliveryProviderType> available = new LinkedHashSet<>(providerCatalog.enabledProviders());
                    DeliveryProviderType defaultProvider = available.contains(DEFAULT_PROVIDER)
                            ? DEFAULT_PROVIDER : available.stream().findFirst().orElse(null);
                    return new StoreDeliveryConfigResponse(storeId, store.businessId(), new ArrayList<>(available),
                            defaultProvider, "SIMULATED", null);
                });
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = DeliveryCacheNames.STORE_CONFIG, key = "#storeId"),
            @CacheEvict(cacheNames = DeliveryCacheNames.STORE_ENABLED_PROVIDERS, key = "#storeId"),
            @CacheEvict(cacheNames = DeliveryCacheNames.STORE_DEFAULT_PROVIDER, key = "#storeId"),
            @CacheEvict(cacheNames = DeliveryCacheNames.STORE_PROVIDER_ENABLED, allEntries = true)
    })
    @Transactional
    public StoreDeliveryConfigResponse update(Long storeId, UpdateStoreDeliveryConfigRequest request) {
        if (!request.enabledProviders().contains(request.defaultProvider())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The default provider must be enabled for this store");
        }
        if (!providerCatalog.enabledProviders().containsAll(request.enabledProviders())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A selected provider is not currently offered by the platform");
        }
        StoreLocationClient.StoreDetails store = storeLocationClient.getStoreDetails(storeId);
        StoreDeliveryConfig config = configRepository.findByStoreId(storeId)
                .orElseGet(() -> StoreDeliveryConfig.builder().storeId(storeId).businessId(store.businessId()).build());
        config.setBusinessId(store.businessId());
        config.setEnabledProviders(new LinkedHashSet<>(request.enabledProviders()));
        config.setDefaultProvider(request.defaultProvider());
        return toResponse(configRepository.save(config));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = DeliveryCacheNames.STORE_ENABLED_PROVIDERS, key = "#storeId")
    public List<DeliveryProviderType> enabledProviders(Long storeId) {
        return configRepository.findByStoreId(storeId)
                .map(config -> {
                    Set<DeliveryProviderType> effective = new LinkedHashSet<>(config.getEnabledProviders());
                    effective.retainAll(providerCatalog.enabledProviders());
                    return effective.stream().sorted().collect(java.util.stream.Collectors.toCollection(ArrayList::new));
                })
                .orElseGet(() -> new ArrayList<>(providerCatalog.enabledProviders()));
    }

    @Cacheable(cacheNames = DeliveryCacheNames.STORE_DEFAULT_PROVIDER, key = "#storeId")
    @Transactional(readOnly = true)
    public DeliveryProviderType defaultProvider(Long storeId) {
        return configRepository.findByStoreId(storeId)
                .map(config -> {
                    Set<DeliveryProviderType> effective = new LinkedHashSet<>(enabledProviders(storeId));
                    if (effective.contains(config.getDefaultProvider())) return config.getDefaultProvider();
                    return effective.stream().findFirst().orElse(null);
                })
                .orElseGet(() -> {
                    Set<DeliveryProviderType> available = new LinkedHashSet<>(providerCatalog.enabledProviders());
                    return available.contains(DEFAULT_PROVIDER) ? DEFAULT_PROVIDER
                            : available.stream().findFirst().orElse(null);
                });
    }

    @Cacheable(cacheNames = DeliveryCacheNames.STORE_PROVIDER_ENABLED,
            key = "#storeId + ':' + #provider.name()")
    @Transactional(readOnly = true)
    public boolean isEnabled(Long storeId, DeliveryProviderType provider) {
        return enabledProviders(storeId).contains(provider);
    }

    private StoreDeliveryConfigResponse toResponse(StoreDeliveryConfig config) {
        Set<DeliveryProviderType> effective = new LinkedHashSet<>(config.getEnabledProviders());
        effective.retainAll(providerCatalog.enabledProviders());
        DeliveryProviderType defaultProvider = effective.contains(config.getDefaultProvider())
                ? config.getDefaultProvider() : effective.stream().findFirst().orElse(null);
        return new StoreDeliveryConfigResponse(config.getStoreId(), config.getBusinessId(),
                effective.stream().sorted().collect(java.util.stream.Collectors.toCollection(ArrayList::new)),
                defaultProvider, "SIMULATED", config.getUpdatedAt());
    }
}
