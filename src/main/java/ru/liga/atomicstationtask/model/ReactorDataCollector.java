package ru.liga.atomicstationtask.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.liga.atomicstationtask.model.entity.GraphiteRod;
import ru.liga.atomicstationtask.model.entity.Reactor;
import ru.liga.atomicstationtask.model.enums.ReactorState;

@Service
public class ReactorDataCollector {

    private final Reactor reactor;
    private final ReactorRegulator regulator;

    @Autowired
    public ReactorDataCollector(ReactorRegulator regulator) {
        this.regulator = regulator;
        this.reactor = new Reactor(new GraphiteRod());
        this.reactor.start();
    }

    @Async
    @Scheduled(fixedRate = 1000)
    public void printReactorData() {
        if (reactor.getState() == ReactorState.DESTROYED) {
            System.out.println("Нет данных, потеряно соединение");
            return;
        }
        int currentPower = reactor.getCurrentPower();
        int currentTemperature = reactor.getCurrentTemperature();
        int percentage = reactor.getGraphiteRod().getImmersionPercentage();
        System.out.printf("Данные собраны: Мощность: %d Вт," +
                        " Температура: %d °C," +
                        " Погружение стержней: %s%n",
                currentPower, currentTemperature, percentage);
    }

    @Async
    @Scheduled(fixedRate = 1000)
    public void sendReactorDataToRegulator() {
        regulator.addPowerRecord(reactor, reactor.getCurrentPower());
    }
}