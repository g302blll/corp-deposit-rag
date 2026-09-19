package com.ljp.corpdeposit.business;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ljp.corpdeposit")
public class DepositBusinessServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DepositBusinessServiceApplication.class, args);
    }
}
