package com.ljp.corpdeposit.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ljp.corpdeposit")
public class DepositProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DepositProductServiceApplication.class, args);
    }
}
