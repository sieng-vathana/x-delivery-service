package com.x.delivery.repository;

import com.x.delivery.entity.DeliveryProviderConfig;
import com.x.delivery.entity.DeliveryProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryProviderConfigRepository extends JpaRepository<DeliveryProviderConfig, DeliveryProviderType> {
}
