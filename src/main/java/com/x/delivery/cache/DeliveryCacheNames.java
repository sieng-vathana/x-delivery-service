package com.x.delivery.cache;

/** Redis cache regions owned by the delivery service. */
public final class DeliveryCacheNames {
    public static final String PROVIDER_CATALOG = "delivery-provider-catalog";
    public static final String ENABLED_PROVIDERS = "delivery-enabled-providers";
    public static final String STORE_CONFIG = "store-delivery-config";
    public static final String STORE_ENABLED_PROVIDERS = "store-delivery-enabled-providers";
    public static final String STORE_DEFAULT_PROVIDER = "store-delivery-default-provider";
    public static final String STORE_PROVIDER_ENABLED = "store-delivery-provider-enabled";

    private DeliveryCacheNames() {
    }
}
