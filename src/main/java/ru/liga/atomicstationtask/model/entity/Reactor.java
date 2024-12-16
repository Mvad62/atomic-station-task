package ru.liga.atomicstationtask.model.entity;

import lombok.Getter;
import ru.liga.atomicstationtask.model.enums.ReactorState;

import java.util.Random;

@Getter
public class Reactor implements Runnable {

    private static final int MAX_POWER = 862; // Максимальная мощность в ваттах
    private static final int SAFE_POWER_LIMIT = 700; // Безопасный предел мощности
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
        System.out.println("Реактор запущен!");
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

    public void update() {
        if (state == ReactorState.RUNNING) {
            int targetPower = calculatePower();
            int targetTemperature = calculateTemperature();

            // Увеличение или уменьшение мощности с учетом случайных колебаний
            if (currentPower < targetPower) {
                currentPower = Math.min(currentPower + RANDOM.nextInt(15) + 5, targetPower); // Увеличение мощности
            } else if (currentPower > targetPower) {
                currentPower = Math.max(currentPower - RANDOM.nextInt(10) + 5, targetPower); // Понижение мощности
            }

            // Увеличение или уменьшение температуры с учетом случайных колебаний
            if (currentTemperature < targetTemperature) {
                currentTemperature = Math.min(currentTemperature + RANDOM.nextInt(5) + 2, targetTemperature); // Увеличение температуры
            } else if (currentTemperature > targetTemperature) {
                currentTemperature = Math.max(currentTemperature - RANDOM.nextInt(3) + 1, targetTemperature); // Понижение температуры
            }

            // Проверка на взрыв
            if (currentPower > MAX_POWER) {
                state = ReactorState.DESTROYED;
                System.err.println("ВЗРЫВ ВЗРЫВ ВЗРЫВ ВЗРЫВ ВЗРЫВ");
            } else if (currentPower > SAFE_POWER_LIMIT) {
                System.out.println("Внимание: мощность близка к пределу!");
            }
        }
    }

    private int calculateUpdateInterval(int temperature) {
        // Формула для вычисления задержки на основе температуры
        // Чем выше температура, тем меньше задержка
        int interval = BASE_UPDATE_INTERVAL - (temperature - 300) * 2; // Уменьшаем интервал на 2 мс за каждые 1 градус выше 300
        return Math.max(interval, 200); // Минимальная задержка 200 мс
    }

    private int calculatePower() {
        int immersionPercentage = graphiteRod.getImmersionPercentage();
        // Мощность зависит от процента погружения (чем меньше погружение, тем больше мощность)
        // Мощность может превышать MAX_POWER, если погружение стержня низкое
        return (int) (MAX_POWER * (1 - immersionPercentage / 100.0) * (1 + RANDOM.nextDouble() * 0.2)); // Добавляем случайный множитель для увеличения мощности
    }

    private int calculateTemperature() {
        // Используем более реалистичную формулу для температуры, которая зависит от мощности
        return (int) (currentPower * 1.4 + 50 + RANDOM.nextInt(20) - 10); // Добавляем случайные колебания
    }

    public void setGraphiteRodImmersion(int immersionPercentage) {
        graphiteRod.setImmersionPercentage(immersionPercentage);
    }
}
