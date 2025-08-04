package com.example.crafteria_server.domain.deliverytracking.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackingWebhookRequest {
    private String carrierId;
    private String trackingNumber;
}
