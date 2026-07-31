package com.x.delivery.service;

import com.x.delivery.dto.*;
import com.x.delivery.entity.*;
import com.x.delivery.provider.*;
import com.x.delivery.repository.DeliveryRepository;
import com.x.delivery.repository.DeliveryQuoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final DeliveryQuoteRepository quoteRepository;
    private final DeliveryPersistenceService persistenceService;
    private final Map<DeliveryProviderType, DeliveryProvider> providers;
    public DeliveryService(DeliveryRepository deliveryRepository, DeliveryQuoteRepository quoteRepository,
                           DeliveryPersistenceService persistenceService, List<DeliveryProvider> providers) {
        this.deliveryRepository = deliveryRepository;
        this.quoteRepository = quoteRepository;
        this.persistenceService = persistenceService;
        this.providers = providers.stream().collect(Collectors.toMap(DeliveryProvider::providerType, Function.identity()));
    }
    public DeliveryResponse create(CreateDeliveryRequest request) {
        Delivery delivery = persistenceService.prepare(request);
        if (delivery.getStatus() == DeliveryStatus.BOOKING
                || delivery.getStatus() == DeliveryStatus.ASSIGNED
                || delivery.getStatus() == DeliveryStatus.PICKED_UP
                || delivery.getStatus() == DeliveryStatus.OUT_FOR_DELIVERY
                || delivery.getStatus() == DeliveryStatus.DELIVERED) {
            return toResponse(delivery);
        }
        delivery = persistenceService.beginBooking(delivery.getId());
        if (delivery.getStatus() != DeliveryStatus.BOOKING) return toResponse(delivery);
        DeliveryProvider provider = providers.get(delivery.getProviderType());
        if (provider == null) {
            persistenceService.markBookingFailed(delivery.getId(), "Provider is not enabled");
            throw new ResponseStatusException(HttpStatus.CONFLICT, delivery.getProviderType() + " is not enabled");
        }
        try {
            DeliveryBooking booking = provider.createBooking(request, "delivery-" + delivery.getId());
            return toResponse(persistenceService.markAssigned(delivery.getId(), booking));
        } catch (RuntimeException exception) {
            persistenceService.markBookingFailed(delivery.getId(), exception.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Delivery provider booking failed");
        }
    }
    @Transactional(readOnly = true)
    public DeliveryResponse get(Long id) { return deliveryRepository.findById(id).map(this::toResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found")); }
    @Transactional
    public DeliveryResponse updateStatus(Long id, UpdateDeliveryStatusRequest request) {
        Delivery delivery = deliveryRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found"));
        if (!isAllowedTransition(delivery.getStatus(), request.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Delivery cannot move from " + delivery.getStatus() + " to " + request.status());
        }
        delivery.setStatus(request.status());
        delivery.setFailureReason(request.failureReason());
        return toResponse(deliveryRepository.save(delivery));
    }
    @Transactional
    public DeliveryResponse recordRemittance(Long id, RecordRemittanceRequest request) {
        Delivery delivery = deliveryRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found"));
        if (delivery.getStatus() != DeliveryStatus.DELIVERED) throw new ResponseStatusException(HttpStatus.CONFLICT, "COD can be remitted only after delivery");
        if (delivery.getRemittedAt() != null) throw new ResponseStatusException(HttpStatus.CONFLICT, "COD was already remitted");
        if (delivery.getCodAmount() != null && request.remittedAmount().compareTo(delivery.getCodAmount()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Remitted amount cannot exceed COD amount");
        }
        delivery.setRemittedAmount(request.remittedAmount());
        delivery.setRemittedAt(LocalDateTime.now());
        return toResponse(deliveryRepository.save(delivery));
    }
    private DeliveryResponse toResponse(Delivery d) { return new DeliveryResponse(d.getId(), d.getOrderId(), d.getStoreId(), d.getProviderType(), d.getStatus(), d.getTrackingNumber(), d.getRiderName(), d.getRiderPhone(), d.getDeliveryFee(), d.getCourierCost(), d.getCodAmount(), d.getRemittedAmount()); }

    private boolean isAllowedTransition(DeliveryStatus current, DeliveryStatus next) {
        if (current == next) return true;
        return switch (current) {
            case PENDING_ASSIGNMENT, FAILED_ATTEMPT -> next == DeliveryStatus.BOOKING || next == DeliveryStatus.CANCELLED;
            case BOOKING -> next == DeliveryStatus.ASSIGNED || next == DeliveryStatus.FAILED_ATTEMPT || next == DeliveryStatus.CANCELLED;
            case ASSIGNED -> next == DeliveryStatus.PICKED_UP || next == DeliveryStatus.CANCELLED;
            case PICKED_UP -> next == DeliveryStatus.OUT_FOR_DELIVERY || next == DeliveryStatus.RETURNED;
            case OUT_FOR_DELIVERY -> next == DeliveryStatus.DELIVERED
                    || next == DeliveryStatus.FAILED_ATTEMPT || next == DeliveryStatus.RETURNED;
            case DELIVERED, RETURNED, CANCELLED -> false;
        };
    }
}
