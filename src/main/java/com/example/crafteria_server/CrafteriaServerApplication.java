package com.example.crafteria_server;

import com.example.crafteria_server.global.portone.PortOneProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@OpenAPIDefinition(
        servers = {
                @Server(url = "/", description = "Default Server url")
        }
)
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(PortOneProps.class)
public class CrafteriaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrafteriaServerApplication.class, args);
    }

}
