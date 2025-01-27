package ru.liga.atomicstationtask.core;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.liga.atomicstationtask.core.model.OldReactorRegulator;
import ru.liga.atomicstationtask.core.model.entity.Reactor;
import ru.liga.atomicstationtask.core.model.enums.ReactorState;

@Component
@RequiredArgsConstructor
public class AppResultHandler {

    private final ApplicationContext context;
    private final Reactor reactor;

    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String RESET = "\u001B[0m";
    private final OldReactorRegulator oldReactorRegulator;

    @Scheduled(initialDelay = 30000)
    public void checkApplicationStatus() {
        boolean isReactorRunning = reactor.getState() != ReactorState.RUNNING;

        if (isReactorRunning) {
            System.out.println(RED + "Ты не справился, исправляй!" + RESET);
        } else {
            System.out.println(GREEN + "Поздравляю! Станция уцелела!" + RESET);
        }
        reactor.stop();
        SpringApplication.exit(context);
    }
}