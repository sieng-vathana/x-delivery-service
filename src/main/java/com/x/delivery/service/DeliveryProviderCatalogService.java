package com.x.delivery.service;

import com.x.delivery.cache.DeliveryCacheNames;
import com.x.delivery.dto.DeliveryProviderOptionResponse;
import com.x.delivery.dto.UpdateDeliveryProvidersRequest;
import com.x.delivery.entity.DeliveryProviderConfig;
import com.x.delivery.entity.DeliveryProviderType;
import com.x.delivery.repository.DeliveryProviderConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DeliveryProviderCatalogService {
    private final DeliveryProviderConfigRepository repository;

    @Cacheable(cacheNames = DeliveryCacheNames.PROVIDER_CATALOG, key = "'all'")
    @Transactional(readOnly = true)
    public List<DeliveryProviderOptionResponse> list() {
        Set<DeliveryProviderType> enabled = Set.copyOf(enabledProviders());
        return new ArrayList<>(EnumSet.allOf(DeliveryProviderType.class).stream()
                .map(provider -> new DeliveryProviderOptionResponse(provider, displayName(provider),
                        enabled.contains(provider), "SIMULATED"))
                .toList());
    }

    @Cacheable(cacheNames = DeliveryCacheNames.ENABLED_PROVIDERS, key = "'all'")
    @Transactional(readOnly = true)
    public List<DeliveryProviderType> enabledProviders() {
        var saved = repository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        DeliveryProviderConfig::getProviderType, DeliveryProviderConfig::isEnabled));
        EnumSet<DeliveryProviderType> enabled = EnumSet.noneOf(DeliveryProviderType.class);
        for (DeliveryProviderType provider : DeliveryProviderType.values()) {
            // Keep providers available by default until the platform admin explicitly configures the catalog.
            if (saved.getOrDefault(provider, true)) enabled.add(provider);
        }
        return new ArrayList<>(enabled);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = DeliveryCacheNames.PROVIDER_CATALOG, allEntries = true),
            @CacheEvict(cacheNames = DeliveryCacheNames.ENABLED_PROVIDERS, allEntries = true),
            @CacheEvict(cacheNames = DeliveryCacheNames.STORE_CONFIG, allEntries = true),
            @CacheEvict(cacheNames = DeliveryCacheNames.STORE_ENABLED_PROVIDERS, allEntries = true),
            @CacheEvict(cacheNames = DeliveryCacheNames.STORE_DEFAULT_PROVIDER, allEntries = true),
            @CacheEvict(cacheNames = DeliveryCacheNames.STORE_PROVIDER_ENABLED, allEntries = true)
    })
    @Transactional
    public List<DeliveryProviderOptionResponse> update(UpdateDeliveryProvidersRequest request) {
        if (request.enabledProviders().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "At least one delivery provider must remain available to merchants");
        }
        List<DeliveryProviderConfig> configs = EnumSet.allOf(DeliveryProviderType.class).stream()
                .map(provider -> repository.findById(provider)
                        .orElseGet(() -> DeliveryProviderConfig.builder().providerType(provider).build()))
                .peek(config -> config.setEnabled(request.enabledProviders().contains(config.getProviderType())))
                .toList();
        repository.saveAll(configs);
        return list();
    }

    private String displayName(DeliveryProviderType provider) {
        return switch (provider) {
            case GRAB -> "GrabExpress";
            case VIREAK_BUNTHAM -> "Vireak Buntham";
            case OWN_RIDER -> "Store rider";
        };
    }
}
