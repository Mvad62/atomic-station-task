package ru.liga.atomicstationtask.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.liga.atomicstationtask.core.model.OldReactorRegulator;
import ru.liga.atomicstationtask.task.monitor.EngineerMonitor;

@Service
@RequiredArgsConstructor
public class SchedulerService {
    private final OldReactorRegulator reactorRegulator;
    private final EngineerMonitor engineerMonitor;

    @Scheduled(fixedDelay = 2000)
    public void regulate() {
        reactorRegulator.receivePowerData(500);
    }

    @Async("taskScheduler")
    @Scheduled(fixedRate = 1000)
    public void monitorReactor() {
        engineerMonitor.print();
    }

    @Async("taskScheduler")
    @Scheduled(fixedRate = 10000)
    public void monitorReactorResult() {
        engineerMonitor.printSummaryReport();
    }
}
