package com.x.delivery.repository;

import com.x.delivery.entity.StoreDeliveryConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreDeliveryConfigRepository extends JpaRepository<StoreDeliveryConfig, Long> {
    Optional<StoreDeliveryConfig> findByStoreId(Long storeId);
}
