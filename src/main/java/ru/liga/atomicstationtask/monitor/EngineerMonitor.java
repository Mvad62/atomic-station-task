package ru.liga.atomicstationtask.monitor;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.liga.atomicstationtask.model.entity.Reactor;
import ru.liga.atomicstationtask.model.enums.ReactorState;

@Component
@RequiredArgsConstructor
public class EngineerMonitor implements Monitor {


    private final Reactor reactor;

    @Override
    public void print() {
        if (reactor.getState().equals(ReactorState.DESTROYED)) {
            System.out.println("Нет данных, потеряно соединение");
            return;
        }
        int currentPower = reactor.getCurrentPower();
        int currentTemperature = reactor.getCurrentTemperature();
        int percentage = reactor.getGraphiteRod().getImmersionPercentage();
        System.out.printf("Данные собраны: Мощность: %d Вт, Температура: %d °C, Погружение стержней: %s%n",
                currentPower, currentTemperature, percentage);
    }
}
