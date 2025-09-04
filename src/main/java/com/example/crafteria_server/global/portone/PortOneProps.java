package com.example.crafteria_server.global.portone;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "portone")
public class PortOneProps {
    private String apiSecret;
    private String storeId;
    private String channelKey;
}

