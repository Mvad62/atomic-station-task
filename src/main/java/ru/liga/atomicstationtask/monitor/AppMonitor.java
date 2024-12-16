package ru.liga.atomicstationtask.monitor;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.liga.atomicstationtask.model.ReactorRegulator;
import ru.liga.atomicstationtask.model.enums.ReactorState;

@Component
@RequiredArgsConstructor
public class AppMonitor {

    private final ApplicationContext context;
    private final ReactorRegulator reactorRegulator;

    @Scheduled(initialDelay = 30000)
    public void checkApplicationStatus() {
        boolean isReactorRunning = reactorRegulator.getPowerRecords().keySet().stream()
                .anyMatch(reactor -> reactor.getState() == ReactorState.DESTROYED);

        if (isReactorRunning) {
            System.out.println("Ты не справился, проверь шедулеры!");
        } else {
            System.out.println("Поздравляю! Станция уцелела!");
        }
        SpringApplication.exit(context, () -> 0);
    }
}
