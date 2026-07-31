package com.x.delivery.provider;

import com.x.delivery.dto.CreateDeliveryRequest;
import com.x.delivery.entity.DeliveryProviderType;
import org.springframework.stereotype.Component;

@Component
public class SimulatedDeliveryProvider implements DeliveryProvider {
    @Override public DeliveryProviderType providerType() { return DeliveryProviderType.OWN_RIDER; }
    @Override public DeliveryBooking createBooking(CreateDeliveryRequest request, String idempotencyKey) {
        return new DeliveryBooking("OWN-" + idempotencyKey, "Own Rider", "000000000");
    }
}
