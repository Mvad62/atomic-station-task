package ru.liga.atomicstationtask.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.liga.atomicstationtask.model.entity.Reactor;

@Slf4j
@Service
public class ReactorDataCollector {

    private final Reactor reactor;
    private final OldReactorRegulator regulator;

    public ReactorDataCollector(Reactor reactor, OldReactorRegulator regulator) {
        this.reactor = reactor;
        this.regulator = regulator;
        reactor.start();
    }

    @Scheduled(fixedRate = 2000)
    public void sendReactorDataToRegulator() {
        regulator.receivePowerData(reactor, reactor.getCurrentPower());
    }
}
