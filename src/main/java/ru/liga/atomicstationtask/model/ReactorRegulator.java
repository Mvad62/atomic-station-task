package ru.liga.atomicstationtask.model;

import lombok.Getter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.liga.atomicstationtask.model.entity.Reactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReactorRegulator {

    private static final int LOWER_LIMIT = 450;
    private static final int BUFFER_SIZE_LIMIT = 5;

    @Getter private final Map<Reactor, List<Integer>> powerRecords;
    private volatile boolean running;
    private volatile boolean reloading;

    public ReactorRegulator() {
        this.powerRecords = new HashMap<>();
        this.running = true;
        this.reloading = false;
    }

    public void addPowerRecord(Reactor reactor, int power) {
        powerRecords.putIfAbsent(reactor, new ArrayList<>());
        List<Integer> records = powerRecords.get(reactor);
        records.add(power);

        if (records.size() > BUFFER_SIZE_LIMIT) {
            handleOverload(reactor);
        }
        workImitation();
    }

    @Scheduled(fixedRate = 2500)
    public void regulateReactors() {
        if (!running || reloading) return;

        for (Map.Entry<Reactor, List<Integer>> entry : powerRecords.entrySet()) {
            Reactor reactor = entry.getKey();
            List<Integer> records = entry.getValue();
            regulateReactor(reactor);
            if (!records.isEmpty()) {
                records.removeFirst();
            }
        }
    }

    private void regulateReactor(Reactor reactor) {
        int newPercent = calculateImmersionPercent(reactor.getCurrentPower());
        reactor.setGraphiteRodImmersion(newPercent);
        workImitation();
    }

    private int calculateImmersionPercent(int currentPower) {
        int percent = currentPower - LOWER_LIMIT;
        if (percent > 100) {
            return 100;
        } else return Math.max(percent, 0);
    }

    private void handleOverload(Reactor reactor) {
        if (!running || reloading) return;
        System.err.println("Сервер регулировки перегружен! Регулирование приостановлено.");
        System.out.println("Перезагрузка системы...");

        reloading = true;
        reactor.setGraphiteRodImmersion(0);
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        powerRecords.clear();
        System.out.println("Система перезагружена. Записи о мощности очищены.");
        reloading = false;
    }

    private void workImitation() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
