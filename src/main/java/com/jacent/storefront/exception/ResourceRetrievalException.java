package com.jacent.storefront.exception;

public class ResourceRetrievalException  extends RuntimeException {
    public ResourceRetrievalException(String message) {
        super(message);
    }

    public ResourceRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
