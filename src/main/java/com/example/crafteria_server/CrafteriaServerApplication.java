package com.example.crafteria_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.scheduling.annotation.EnableScheduling;

@OpenAPIDefinition(
        servers = {
                @Server(url = "/", description = "Default Server url")
        }
)
@SpringBootApplication
@EnableScheduling
public class CrafteriaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrafteriaServerApplication.class, args);
    }

}
