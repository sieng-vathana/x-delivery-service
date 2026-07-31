package com.x.delivery.provider;
import com.x.delivery.dto.CreateDeliveryRequest;
import com.x.delivery.entity.DeliveryProviderType;
import org.springframework.stereotype.Component;
@Component
public class SimulatedVireakBunthamProvider implements DeliveryProvider {
    public DeliveryProviderType providerType() { return DeliveryProviderType.VIREAK_BUNTHAM; }
    public DeliveryBooking createBooking(CreateDeliveryRequest request, String idempotencyKey) { return new DeliveryBooking("VB-SIM-" + idempotencyKey, "Simulated Vireak Buntham Rider", "000000000"); }
}
