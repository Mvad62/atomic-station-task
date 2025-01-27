package ru.liga.atomicstationtask.core.model;

import org.springframework.stereotype.Component;
import ru.liga.atomicstationtask.core.model.entity.Reactor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Регулятор реактора старого образца.
 * В связи с особенностями работы регулятора, необходимо учитывать следующие нюансы:
 * <ul>
 *     <li>Необходимо регулярно отсылать данные для стабильной работы реактора и регулятора.</li>
 *     <li>Если данные после перезагрузки не поступят в течение 500 мс, то регулятор выйдет из строя.</li>
 *     <li>Обработка данных занимает неопределенный период времени, но после обработки идет перезагрузка,
 *         которая занимает 2 секунды (или 2000 мс).</li>
 *     <li>Если новые данные поступят во время обработки предыдущих или во время перезагрузки,
 *         то регулятор выйдет из строя.</li>
 * </ul>
 *
 * Принцип работы следующий:
 * <ol>
 *     <li>Поступили новые данные</li>
 *     <li>Обработка (n времени)</li>
 *     <li>Перезагрузка (2 сек)</li>
 *     <li>Ожидание (500 мс)</li>
 * </ol>
 */
@Component
public class OldReactorRegulator {

    private static final int LOWER_LIMIT = 450;
    private final Reactor reactor;
    private volatile boolean running;
    private final Lock lock;
    private boolean isUpdated = false;

    public OldReactorRegulator(Reactor reactor) {
        this.running = true;
        this.lock = new ReentrantLock();
        this.reactor = reactor;
        reactor.start();
    }

    public void receivePowerData(int power) {
        System.out.println("Поступили новые данные, идет обработка...");
        isUpdated = true;
        if (!isRegulatorActive()) {
            return;
        }

        if (!lock.tryLock()) {
            System.err.println("Ошибка: Переполнение регулятора, необходимо вызывать метод ТОЛЬКО после выполнения.");
            return;
        }

        try {
            regulateReactor(power);
            new Thread(this::restAndWaitForData).start();
            isUpdated = false;
            System.out.println("Данные обработаны, перезагрузка...");
        } finally {
            lock.unlock();
        }
    }

    private boolean isRegulatorActive() {
        if (!running) {
            System.err.println("Регулятор неисправен, операция невозможна.");
            return false;
        }
        return true;
    }

    private void regulateReactor(int currentPower) {
        int newImmersionPercent = calculateImmersionPercent(currentPower);
        reactor.setGraphiteRodImmersion(newImmersionPercent);
        imitateWork();
    }

    private int calculateImmersionPercent(int currentPower) {
        int percent = currentPower - LOWER_LIMIT;
        if (percent > 100) {
            return 100;
        }
        return Math.max(percent, 0);
    }

    private void imitateWork() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected void restAndWaitForData() {
        try {
            Thread.sleep(1900);
            if (isUpdated && running) {
                handleError("Ошибка: данные поступили слишком быстро, регулятор не успел перезагрузиться.");
                return;
            }
            System.out.println("Ожидаются данные...");
            Thread.sleep(500);

            if (isUpdated && running) {
                isUpdated = false;
                return;
            }

            handleError("Ошибка: данные не поступили в течение 500 мс.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void handleError(String message) {
        System.err.println(message);
        reactor.setGraphiteRodImmersion(0);
        running = false;
    }
}
