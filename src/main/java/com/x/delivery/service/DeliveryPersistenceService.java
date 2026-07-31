package com.x.delivery.service;

import com.x.delivery.dto.CreateDeliveryRequest;
import com.x.delivery.entity.*;
import com.x.delivery.provider.DeliveryBooking;
import com.x.delivery.repository.DeliveryQuoteRepository;
import com.x.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeliveryPersistenceService {
    private final DeliveryRepository deliveryRepository;
    private final DeliveryQuoteRepository quoteRepository;

    @Transactional
    public Delivery prepare(CreateDeliveryRequest request) {
        Delivery idempotent = deliveryRepository.findByIdempotencyKey(request.idempotencyKey()).orElse(null);
        if (idempotent != null) {
            return idempotent;
        }
        Delivery existing = deliveryRepository.findByOrderId(request.orderId()).orElse(null);
        if (existing != null) {
            if (!existing.getQuoteId().equals(request.quoteId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Order already has a delivery using another quote");
            }
            if (existing.getStatus() == DeliveryStatus.ASSIGNED
                    || existing.getStatus() == DeliveryStatus.BOOKING
                    || existing.getStatus() == DeliveryStatus.PICKED_UP
                    || existing.getStatus() == DeliveryStatus.OUT_FOR_DELIVERY
                    || existing.getStatus() == DeliveryStatus.DELIVERED) {
                return existing;
            }
            existing.setStatus(DeliveryStatus.PENDING_ASSIGNMENT);
            existing.setFailureReason(null);
            return deliveryRepository.save(existing);
        }

        DeliveryQuote quote = quoteRepository.findByIdForUpdate(request.quoteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery quote not found"));
        if (quote.getStatus() != DeliveryQuoteStatus.ACTIVE || quote.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Delivery quote has expired or was already selected");
        }
        quote.setStatus(DeliveryQuoteStatus.SELECTED);
        quoteRepository.save(quote);
        return deliveryRepository.save(Delivery.builder()
                .orderId(request.orderId()).storeId(quote.getStoreId()).quoteId(quote.getId())
                .idempotencyKey(request.idempotencyKey())
                .providerType(quote.getProviderType()).status(DeliveryStatus.PENDING_ASSIGNMENT)
                .deliveryFee(quote.getQuotedFee()).courierCost(quote.getQuotedFee())
                .codAmount(request.codAmount()).build());
    }

    @Transactional
    public Delivery markAssigned(Long deliveryId, DeliveryBooking booking) {
        Delivery delivery = require(deliveryId);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setTrackingNumber(booking.trackingNumber());
        delivery.setRiderName(booking.riderName());
        delivery.setRiderPhone(booking.riderPhone());
        delivery.setFailureReason(null);
        return deliveryRepository.save(delivery);
    }

    @Transactional
    public Delivery beginBooking(Long deliveryId) {
        Delivery delivery = require(deliveryId);
        if (delivery.getStatus() != DeliveryStatus.PENDING_ASSIGNMENT
                && delivery.getStatus() != DeliveryStatus.FAILED_ATTEMPT) {
            return delivery;
        }
        delivery.setStatus(DeliveryStatus.BOOKING);
        delivery.setFailureReason(null);
        return deliveryRepository.saveAndFlush(delivery);
    }

    @Transactional
    public void markBookingFailed(Long deliveryId, String reason) {
        Delivery delivery = require(deliveryId);
        delivery.setStatus(DeliveryStatus.FAILED_ATTEMPT);
        delivery.setFailureReason(reason == null ? "Provider booking failed" : reason.substring(0, Math.min(500, reason.length())));
        deliveryRepository.save(delivery);
    }

    private Delivery require(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found"));
    }
}
