package ru.liga.atomicstationtask;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class AtomicStationTaskApplication {

    public static void main(String[] args) {
        log.info("Приложение запущено");
        SpringApplication.run(AtomicStationTaskApplication.class, args);
    }
}
