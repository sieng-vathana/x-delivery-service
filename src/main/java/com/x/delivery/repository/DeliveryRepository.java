package com.x.delivery.repository;
import com.x.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrderId(Long orderId);
    Optional<Delivery> findByIdempotencyKey(String idempotencyKey);
}
