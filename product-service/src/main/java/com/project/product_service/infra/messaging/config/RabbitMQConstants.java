package com.project.product_service.infra.messaging.config;

public class RabbitMQConstants {
    public static final String PRODUCT_EXCHANGE = "product.exchange";
    public static final String DLX_EXCHANGE = "dlx.exchange";

    public static final String PRODUCT_CREATED = "product.created";
    public static final String PRODUCT_CREATED_QUEUE = "product.created.queue";

    public static final String PRODUCT_EDITED = "product.edited";
    public static final String PRODUCT_EDITED_QUEUE = "product.edited.queue";

    public static final String PRODUCT_DELETED = "product.deleted";
    public static final String PRODUCT_DELETED_QUEUE = "product.deleted.queue";
}
