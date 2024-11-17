package com.example.crafteria_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        servers = {
                @Server(url = "/", description = "Default Server url")
        }
)
@SpringBootApplication
public class CrafteriaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrafteriaServerApplication.class, args);
    }

}
