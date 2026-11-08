package com.fulvo.operaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.fulvo.operaciones", "com.fulvo.common"})
public class OperacionesPartidosApplication {

    public static void main(String[] args) {
        SpringApplication.run(OperacionesPartidosApplication.class, args);
    }
}
