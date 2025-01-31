## АЭС

#### На атоманой станции возникла проблема, данные не отправляются в регулятор мощности реактора.

### Задачи

1. Реализовать класс с шедулером, который будет регулярно отсылать данные о мощности реактора в класс [OldReactorRegulator](src/main/java/ru/liga/atomicstationtask/core/model/OldReactorRegulator.java), рекомендуется прочитать документацию к классу.
2. Реализовать [Монитор](src/main/java/ru/liga/atomicstationtask/task/monitor/Monitor.java), который будет собирать ежесекундные данные о [реакторе](src/main/java/ru/liga/atomicstationtask/core/model/entity/Reactor.java) и выводить их общим списком каждые 10 секунд.

 - пакет core модифицировать запрещено