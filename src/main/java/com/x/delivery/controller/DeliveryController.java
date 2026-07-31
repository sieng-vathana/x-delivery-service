package com.x.delivery.controller;
import com.sharedlib.response.ApiResponse;
import com.x.delivery.dto.*;
import com.x.delivery.service.DeliveryService;
import com.x.delivery.service.DeliveryQuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/deliveries") @RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService deliveryService;
    private final DeliveryQuoteService deliveryQuoteService;
    @PostMapping("/quotes") public ResponseEntity<ApiResponse<java.util.List<DeliveryQuoteResponse>>> createQuotes(@Valid @RequestBody CreateDeliveryQuoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED.value(), "Delivery quotes created", deliveryQuoteService.createQuotes(request))); }
    @GetMapping("/quotes/{id}") public ResponseEntity<ApiResponse<DeliveryQuoteResponse>> getActiveQuote(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), deliveryQuoteService.requireActiveQuote(id))); }
    @PostMapping public ResponseEntity<ApiResponse<DeliveryResponse>> create(@Valid @RequestBody CreateDeliveryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED.value(), "Delivery created", deliveryService.create(request))); }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<DeliveryResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), deliveryService.get(id))); }
    @PostMapping("/{id}/status") public ResponseEntity<ApiResponse<DeliveryResponse>> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateDeliveryStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Delivery status updated", deliveryService.updateStatus(id, request))); }
    @PostMapping("/{id}/remittance") public ResponseEntity<ApiResponse<DeliveryResponse>> recordRemittance(@PathVariable Long id, @Valid @RequestBody RecordRemittanceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "COD remittance recorded", deliveryService.recordRemittance(id, request))); }
}
