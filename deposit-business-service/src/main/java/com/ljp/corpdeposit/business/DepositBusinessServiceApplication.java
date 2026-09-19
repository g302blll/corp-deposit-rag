package com.ljp.corpdeposit.business;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.ljp.corpdeposit.business")
@SpringBootApplication(scanBasePackages = "com.ljp.corpdeposit")
public class DepositBusinessServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DepositBusinessServiceApplication.class, args);
    }
}

