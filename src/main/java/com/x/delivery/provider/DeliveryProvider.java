package com.x.delivery.provider;
import com.x.delivery.dto.CreateDeliveryRequest;
import com.x.delivery.entity.DeliveryProviderType;
public interface DeliveryProvider {
    DeliveryProviderType providerType();
    DeliveryBooking createBooking(CreateDeliveryRequest request, String idempotencyKey);
}
