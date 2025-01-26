package ru.liga.atomicstationtask.model.entity;

import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.liga.atomicstationtask.model.enums.ReactorState;

import java.util.Random;

@Getter
@Component
public class Reactor implements Runnable {

    private static final int MAX_POWER = 862; // Максимальная мощность в ваттах
    private static final int BASE_UPDATE_INTERVAL = 1000; // Базовый интервал обновления в миллисекундах
    private static final Random RANDOM = new Random();

    private final GraphiteRod graphiteRod;
    private int currentPower = 450; // Текущая мощность в ваттах
    private int currentTemperature = 440; // Текущая температура в градусах Цельсия
    private ReactorState state;

    public Reactor(GraphiteRod graphiteRod) {
        this.graphiteRod = graphiteRod;
        this.state = ReactorState.STOPPED;
    }

    public void start() {
        if (state == ReactorState.STOPPED) {
            state = ReactorState.RUNNING;
            new Thread(this).start();
        }
    }

    public void stop() {
        if (state == ReactorState.RUNNING) {
            state = ReactorState.STOPPED;
            currentPower = 0;
            currentTemperature = 0;
        }
    }

    @Override
    public void run() {
        while (state == ReactorState.RUNNING) {
            update();
            try {
                int updateInterval = calculateUpdateInterval(currentTemperature);
                Thread.sleep(updateInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void update() {
        if (state == ReactorState.RUNNING) {
            int targetPower = calculatePower();
            int targetTemperature = calculateTemperature();

            // Увеличение или уменьшение мощности
            if (currentPower < targetPower) {
                currentPower = Math.min(currentPower + RANDOM.nextInt(15) + 5, targetPower);
            } else if (currentPower > targetPower) {
                currentPower = Math.max(currentPower - RANDOM.nextInt(10) + 5, targetPower);
            }

            // Увеличение или уменьшение температуры
            if (currentTemperature < targetTemperature) {
                currentTemperature = Math.min(currentTemperature + RANDOM.nextInt(5) + 2, targetTemperature);
            } else if (currentTemperature > targetTemperature) {
                currentTemperature = Math.max(currentTemperature - RANDOM.nextInt(3) + 1, targetTemperature);
            }

            // Проверка на взрыв
            if (currentPower > MAX_POWER) {
                state = ReactorState.DESTROYED;
                System.err.println("ВЗРЫВ: Мощность превысила максимальный предел!");
            }
        }
    }

    private int calculateUpdateInterval(int temperature) {
        int interval = BASE_UPDATE_INTERVAL - (temperature - 300) * 2;
        return Math.max(interval, 200);
    }

    private int calculatePower() {
        int immersionPercentage = graphiteRod.getImmersionPercentage();
        return (int) (MAX_POWER * (1 - immersionPercentage / 100.0) * (1 + RANDOM.nextDouble() * 0.2));
    }

    private int calculateTemperature() {
        return (int) (currentPower * 1.4 + 50 + RANDOM.nextInt(20) - 10);
    }

    public void setGraphiteRodImmersion(int immersionPercentage) {
        graphiteRod.setImmersionPercentage(immersionPercentage);
    }
}
