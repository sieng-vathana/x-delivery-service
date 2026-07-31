package com.x.delivery.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
public record CreateDeliveryRequest(@NotNull @Positive Long orderId, @NotNull @Positive Long quoteId,
                                    BigDecimal codAmount, @NotBlank String idempotencyKey) { }
