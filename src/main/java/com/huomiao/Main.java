package com.huomiao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = "com.huomiao")
@SpringBootConfiguration()
@Slf4j
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class,args);
        log.info("\n __    __   __    __    ______          .___  ___.  __       ___       ______   \n" +
                "|  |  |  | |  |  |  |  /  __  \\         |   \\/   | |  |     /   \\     /  __  \\  \n" +
                "|  |__|  | |  |  |  | |  |  |  |  ______|  \\  /  | |  |    /  ^  \\   |  |  |  | \n" +
                "|   __   | |  |  |  | |  |  |  | |______|  |\\/|  | |  |   /  /_\\  \\  |  |  |  | \n" +
                "|  |  |  | |  `--'  | |  `--'  |        |  |  |  | |  |  /  _____  \\ |  `--'  | \n" +
                "|__|  |__|  \\______/   \\______/         |__|  |__| |__| /__/     \\__\\ \\______/  \n" +
                "                                                                                ");
    }

}