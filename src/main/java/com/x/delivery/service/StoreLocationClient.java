package com.x.delivery.service;

import com.sharedlib.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;

@Component
public class StoreLocationClient {
    private final RestClient storeClient;
    public StoreLocationClient(RestClient.Builder builder, @Value("${services.store.base-url}") String baseUrl) {
        this.storeClient = builder.baseUrl(baseUrl).build();
    }
    public StoreCoordinates getCoordinates(Long storeId) {
        StoreDetails store = getStoreDetails(storeId);
        if (store.latitude() == null || store.longitude() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Store must have latitude and longitude before delivery can be quoted");
        }
        return new StoreCoordinates(store.latitude(), store.longitude());
    }
    public StoreDetails getStoreDetails(Long storeId) {
        try {
            ApiResponse<StoreLocationResponse> response = storeClient.get().uri("/api/v1/stores/{id}", storeId)
                    .retrieve().body(new ParameterizedTypeReference<ApiResponse<StoreLocationResponse>>() {});
            if (response == null || response.getData() == null || response.getData().businessId() == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Store was not found");
            }
            StoreLocationResponse data = response.getData();
            return new StoreDetails(data.id(), data.businessId(), data.latitude(), data.longitude());
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Store service is unavailable for delivery settings");
        }
    }
    public record StoreCoordinates(BigDecimal latitude, BigDecimal longitude) { }
    public record StoreDetails(Long id, Long businessId, BigDecimal latitude, BigDecimal longitude) { }
    private record StoreLocationResponse(Long id, Long businessId, BigDecimal latitude, BigDecimal longitude) { }
}
