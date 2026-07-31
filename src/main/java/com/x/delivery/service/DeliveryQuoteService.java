package com.x.delivery.service;

import com.x.delivery.dto.CreateDeliveryQuoteRequest;
import com.x.delivery.dto.DeliveryQuoteResponse;
import com.x.delivery.entity.*;
import com.x.delivery.repository.DeliveryQuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service @RequiredArgsConstructor
public class DeliveryQuoteService {
    private static final BigDecimal EARTH_RADIUS_KM = new BigDecimal("6371");
    private final DeliveryQuoteRepository quoteRepository;
    private final StoreLocationClient storeLocationClient;

    @Transactional
    public List<DeliveryQuoteResponse> createQuotes(CreateDeliveryQuoteRequest request) {
        StoreLocationClient.StoreCoordinates pickup = storeLocationClient.getCoordinates(request.storeId());
        BigDecimal distance = distanceKm(pickup.latitude(), pickup.longitude(), request.destinationLatitude(), request.destinationLongitude());
        String currency = request.currencyCode().toUpperCase(Locale.ROOT);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);
        return List.of(DeliveryProviderType.OWN_RIDER, DeliveryProviderType.GRAB, DeliveryProviderType.VIREAK_BUNTHAM).stream()
                .map(provider -> quoteRepository.save(DeliveryQuote.builder().storeId(request.storeId()).providerType(provider)
                        .pickupLatitude(pickup.latitude()).pickupLongitude(pickup.longitude())
                        .destinationLatitude(request.destinationLatitude()).destinationLongitude(request.destinationLongitude())
                        .distanceKm(distance).quotedFee(feeFor(provider, distance)).currencyCode(currency)
                        .status(DeliveryQuoteStatus.ACTIVE).expiresAt(expiresAt).build()))
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DeliveryQuoteResponse requireActiveQuote(Long quoteId) {
        DeliveryQuote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery quote not found"));
        if (quote.getStatus() != DeliveryQuoteStatus.ACTIVE || !quote.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Delivery quote has expired or was selected");
        }
        return toResponse(quote);
    }
    private BigDecimal feeFor(DeliveryProviderType provider, BigDecimal distanceKm) {
        BigDecimal includedKm = new BigDecimal("3");
        BigDecimal extraKm = distanceKm.subtract(includedKm).max(BigDecimal.ZERO);
        return (switch (provider) {
            case OWN_RIDER -> new BigDecimal("1.00").add(extraKm.multiply(new BigDecimal("0.20")));
            case GRAB -> new BigDecimal("2.20").add(extraKm.multiply(new BigDecimal("0.35")));
            case VIREAK_BUNTHAM -> new BigDecimal("3.00").add(extraKm.multiply(new BigDecimal("0.25")));
        }).setScale(2, RoundingMode.HALF_UP);
    }
    private BigDecimal distanceKm(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        double p1 = Math.toRadians(lat1.doubleValue()), p2 = Math.toRadians(lat2.doubleValue());
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(p1) * Math.cos(p2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_KM.multiply(BigDecimal.valueOf(2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)))).setScale(2, RoundingMode.HALF_UP);
    }
    private DeliveryQuoteResponse toResponse(DeliveryQuote quote) { return new DeliveryQuoteResponse(quote.getId(), quote.getStoreId(), quote.getProviderType(), quote.getDistanceKm(), quote.getQuotedFee(), quote.getCurrencyCode(), quote.getExpiresAt()); }
}
