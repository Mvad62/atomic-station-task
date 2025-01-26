package ru.liga.atomicstationtask.model.entity;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

// Класс, представляющий графитовый стержень, используемый для регулирования мощности реактора
@Getter
@Component
public class GraphiteRod {

    /* Процент погружения стержней в реактор
       Напрямую влияет на мощность реактора
       Может быть от 0 до 100%.*/
    private int immersionPercentage = 0;

    public void setImmersionPercentage(int immersionPercentage) {
        if (immersionPercentage < 0 || immersionPercentage > 100) {
            throw new IllegalArgumentException("Процент погружения должен быть от 0 до 100.");
        }
        this.immersionPercentage = immersionPercentage;
    }
}
