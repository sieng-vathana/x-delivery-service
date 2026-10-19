package com.x.delivery.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "store_delivery_configs", uniqueConstraints =
        @UniqueConstraint(name = "uk_store_delivery_config_store", columnNames = "store_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreDeliveryConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "store_delivery_config_providers",
            joinColumns = @JoinColumn(name = "config_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 32)
    @Builder.Default
    private Set<DeliveryProviderType> enabledProviders = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "default_provider", nullable = false, length = 32)
    private DeliveryProviderType defaultProvider;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
