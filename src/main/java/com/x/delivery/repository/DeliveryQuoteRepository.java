package com.x.delivery.repository;
import com.x.delivery.entity.DeliveryQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface DeliveryQuoteRepository extends JpaRepository<DeliveryQuote, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from DeliveryQuote q where q.id = :id")
    Optional<DeliveryQuote> findByIdForUpdate(@Param("id") Long id);
}
