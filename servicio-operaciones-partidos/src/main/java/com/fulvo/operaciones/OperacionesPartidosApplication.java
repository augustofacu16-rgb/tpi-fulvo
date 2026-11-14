package com.fulvo.operaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.fulvo.operaciones", "com.fulvo.common"})
@EnableScheduling
public class OperacionesPartidosApplication {

    public static void main(String[] args) {
        SpringApplication.run(OperacionesPartidosApplication.class, args);
    }
}
