package com.x.delivery.service;

import com.x.delivery.dto.DeliveryProviderOptionResponse;
import com.x.delivery.dto.UpdateDeliveryProvidersRequest;
import com.x.delivery.entity.DeliveryProviderConfig;
import com.x.delivery.entity.DeliveryProviderType;
import com.x.delivery.repository.DeliveryProviderConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DeliveryProviderCatalogService {
    private final DeliveryProviderConfigRepository repository;

    @Transactional(readOnly = true)
    public List<DeliveryProviderOptionResponse> list() {
        Set<DeliveryProviderType> enabled = enabledProviders();
        return EnumSet.allOf(DeliveryProviderType.class).stream()
                .map(provider -> new DeliveryProviderOptionResponse(provider, displayName(provider),
                        enabled.contains(provider), "SIMULATED"))
                .toList();
    }

    @Transactional(readOnly = true)
    public Set<DeliveryProviderType> enabledProviders() {
        var saved = repository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        DeliveryProviderConfig::getProviderType, DeliveryProviderConfig::isEnabled));
        EnumSet<DeliveryProviderType> enabled = EnumSet.noneOf(DeliveryProviderType.class);
        for (DeliveryProviderType provider : DeliveryProviderType.values()) {
            // Keep providers available by default until the platform admin explicitly configures the catalog.
            if (saved.getOrDefault(provider, true)) enabled.add(provider);
        }
        return Set.copyOf(enabled);
    }

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
