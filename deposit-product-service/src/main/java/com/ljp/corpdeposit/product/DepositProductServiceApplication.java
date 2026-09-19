package com.ljp.corpdeposit.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.ljp.corpdeposit.product")
@SpringBootApplication(scanBasePackages = "com.ljp.corpdeposit")
public class DepositProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DepositProductServiceApplication.class, args);
    }
}

