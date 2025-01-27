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

@Component
@RequiredArgsConstructor
public class EngineerMonitor implements Monitor, Job {

    private static final String FILE_PATH = "monitor_output.txt";
    private final Reactor reactor;

    @Override
    public void print() {
        if (reactor.getState().equals(ReactorState.DESTROYED)) {
            writeToFile("Нет данных, потеряно соединение");
            return;
        }
        int currentPower = reactor.getCurrentPower();
        int currentTemperature = reactor.getCurrentTemperature();
        int percentage = reactor.getGraphiteRod().getImmersionPercentage();
        String output = String.format("Данные собраны: Мощность: %d Вт, Температура: %d °C, Погружение стержней: %s%n",
                currentPower, currentTemperature, percentage);

        writeToFile(output);
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
