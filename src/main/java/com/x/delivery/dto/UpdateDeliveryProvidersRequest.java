package com.x.delivery.dto;

import com.x.delivery.entity.DeliveryProviderType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UpdateDeliveryProvidersRequest(@NotEmpty Set<@NotNull DeliveryProviderType> enabledProviders) {
}
