package ru.study.flights.flight;

import java.time.Duration;
import java.time.Instant;
/**
 * всё, что имеет момент отправления и прибытия.
 */
public interface Timed {

    /** @return момент отправления */
    Instant departure();

    /** @return момент прибытия */
    Instant arrival();

    /** @return фактическая длительность от отправления до прибытия */
    default Duration duration() {
        return Duration.between(departure(), arrival());
    }
}