package ru.liga.atomicstationtask.monitor;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.liga.atomicstationtask.model.entity.Reactor;
import ru.liga.atomicstationtask.model.enums.ReactorState;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SummaryMonitor implements Monitor {

    private final Reactor reactor;
    private final List<String> archive = new ArrayList<>();
    private final long INTERVAL = 10000;
    private long lastTimeChecked = System.currentTimeMillis();

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

        archive.add(String.format("Мощность: %d Вт, Температура: %d °C, Погружение стержней: %s",
                currentPower, currentTemperature, percentage));

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTimeChecked >= INTERVAL) {
            printArchive();
            lastTimeChecked = currentTime;
        }
    }

    private void printArchive() {
        System.out.println("Данные за последние 10 секунд:");
        for (String data : archive) {
            System.out.println(data);
        }
        archive.clear();
    }

    @Scheduled(initialDelay = 5000, fixedRate = 1000)
    public void saveInfo() {
        if (reactor.getState() == ReactorState.DESTROYED) {
            System.out.println("Нет данных, потеряно соединение");
            print();
        }
        int currentPower = reactor.getCurrentPower();
        int currentTemperature = reactor.getCurrentTemperature();
        int percentage = reactor.getGraphiteRod().getImmersionPercentage();
    }
}
