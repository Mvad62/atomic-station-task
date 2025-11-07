package ru.liga.atomicstationtask.task.monitor;

import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import ru.liga.atomicstationtask.core.model.entity.Reactor;
import ru.liga.atomicstationtask.core.model.enums.ReactorState;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
public class EngineerMonitor implements Monitor, Job {

    private static final String FILE_PATH = "monitor_output.txt";
    private final Reactor reactor;

    // Потокобезопасный список для хранения данных измерений
    private final List<MeasurementData> measurementBuffer = new CopyOnWriteArrayList<>();

    // Форматтер для времени
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Внутренний класс для хранения данных измерений
    @RequiredArgsConstructor
    private static class MeasurementData {
        private final LocalDateTime timestamp;
        private final int power;
        private final int temperature;
        private final int rodImmersion;
        private final ReactorState state;
    }

    @Override
    public void print() {
        // Собираем текущие данные и добавляем в буфер
        LocalDateTime currentTime = LocalDateTime.now();

        if (reactor.getState().equals(ReactorState.DESTROYED)) {
            // Если реактор уничтожен, добавляем запись об ошибке
            measurementBuffer.add(new MeasurementData(currentTime, 0, 0, 0, ReactorState.DESTROYED));
        } else {
            int currentPower = reactor.getCurrentPower();
            int currentTemperature = reactor.getCurrentTemperature();
            int percentage = reactor.getGraphiteRod().getImmersionPercentage();

            measurementBuffer.add(new MeasurementData(
                    currentTime,
                    currentPower,
                    currentTemperature,
                    percentage,
                    reactor.getState()
            ));
        }
    }

    public void printSummaryReport() {
        if (measurementBuffer.isEmpty()) {
            return;
        }

        StringBuilder report = new StringBuilder();
        report.append("\n=== ОТЧЕТ ЗА 10 СЕКУНД ===\n");
        report.append(String.format("Период: %s - %s%n",
                measurementBuffer.getFirst().timestamp.format(TIME_FORMATTER),
                measurementBuffer.getLast().timestamp.format(TIME_FORMATTER)
        ));

        // Проверяем, был ли реактор уничтожен в течение этого периода
        boolean wasDestroyed = measurementBuffer.stream()
                .anyMatch(data -> data.state == ReactorState.DESTROYED);

        if (wasDestroyed) {
            report.append("СТАТУС: Реактор уничтожен - нет данных\n");
        } else {
            // Статистика по мощности
            int minPower = measurementBuffer.stream().mapToInt(data -> data.power).min().orElse(0);
            int maxPower = measurementBuffer.stream().mapToInt(data -> data.power).max().orElse(0);
            double avgPower = measurementBuffer.stream().mapToInt(data -> data.power).average().orElse(0);

            // Статистика по температуре
            int minTemp = measurementBuffer.stream().mapToInt(data -> data.temperature).min().orElse(0);
            int maxTemp = measurementBuffer.stream().mapToInt(data -> data.temperature).max().orElse(0);
            double avgTemp = measurementBuffer.stream().mapToInt(data -> data.temperature).average().orElse(0);

            // Статистика по погружению стержней
            int minRod = measurementBuffer.stream().mapToInt(data -> data.rodImmersion).min().orElse(0);
            int maxRod = measurementBuffer.stream().mapToInt(data -> data.rodImmersion).max().orElse(0);
            double avgRod = measurementBuffer.stream().mapToInt(data -> data.rodImmersion).average().orElse(0);

            report.append(String.format("Мощность: мин=%d Вт, макс=%d Вт, ср=%.1f Вт%n", minPower, maxPower, avgPower));
            report.append(String.format("Температура: мин=%d °C, макс=%d °C, ср=%.1f °C%n", minTemp, maxTemp, avgTemp));
            report.append(String.format("Погружение стержней: мин=%d%%, макс=%d%%, ср=%.1f%%%n", minRod, maxRod, avgRod));
            report.append(String.format("Количество измерений: %d%n", measurementBuffer.size()));
        }

        report.append("=== КОНЕЦ ОТЧЕТА ===\n");

        writeToFile(report.toString());
        measurementBuffer.clear();
    }

    private void writeToFile(String message) {
        try (FileWriter fileWriter = new FileWriter(FILE_PATH, true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.print(message);
        } catch (IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        print();
    }
}