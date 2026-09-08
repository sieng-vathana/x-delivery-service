package com.x.delivery.repository;
import com.x.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long>, JpaSpecificationExecutor<Delivery> {
    Optional<Delivery> findByOrderId(Long orderId);
    Optional<Delivery> findByIdempotencyKey(String idempotencyKey);
}
