package com.x.delivery.controller;

import com.sharedlib.response.ApiResponse;
import com.sharedlib.response.PageResponse;
import com.x.delivery.dto.*;
import com.x.delivery.entity.DeliveryProviderType;
import com.x.delivery.entity.DeliveryStatus;
import com.x.delivery.service.DeliveryService;
import com.x.delivery.service.DeliveryQuoteService;
import com.x.delivery.service.StoreDeliveryConfigService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService deliveryService;
    private final DeliveryQuoteService deliveryQuoteService;
    private final StoreDeliveryConfigService storeDeliveryConfigService;

    @GetMapping("/stores/{storeId}/config")
    public ResponseEntity<ApiResponse<StoreDeliveryConfigResponse>> getStoreConfig(@PathVariable Long storeId) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), storeDeliveryConfigService.get(storeId)));
    }

    @PutMapping("/stores/{storeId}/config")
    public ResponseEntity<ApiResponse<StoreDeliveryConfigResponse>> updateStoreConfig(
            @PathVariable Long storeId, @Valid @RequestBody UpdateStoreDeliveryConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Store delivery settings saved",
                storeDeliveryConfigService.update(storeId, request)));
    }

    @PostMapping("/quotes")
    public ResponseEntity<ApiResponse<java.util.List<DeliveryQuoteResponse>>> createQuotes(@Valid @RequestBody CreateDeliveryQuoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Delivery quotes created", deliveryQuoteService.createQuotes(request)));
    }

    @GetMapping("/quotes/{id}")
    public ResponseEntity<ApiResponse<DeliveryQuoteResponse>> getActiveQuote(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), deliveryQuoteService.requireActiveQuote(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryResponse>> create(@Valid @RequestBody CreateDeliveryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Delivery created", deliveryService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DeliveryResponse>>> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) DeliveryStatus status,
            @RequestParam(required = false) DeliveryProviderType providerType,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        Page<DeliveryResponse> result = deliveryService.list(storeId, orderId, status, providerType, search, page, size);
        return ResponseEntity.ok(ApiResponse.success(200, new PageResponse<>(
                result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements(),
                result.getTotalPages(), result.hasNext())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), deliveryService.get(id)));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DeliveryResponse>> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateDeliveryStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Delivery status updated", deliveryService.updateStatus(id, request)));
    }

    @PostMapping("/{id}/remittance")
    public ResponseEntity<ApiResponse<DeliveryResponse>> recordRemittance(@PathVariable Long id, @Valid @RequestBody RecordRemittanceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "COD remittance recorded", deliveryService.recordRemittance(id, request)));
    }
}
