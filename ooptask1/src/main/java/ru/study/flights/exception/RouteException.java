package ru.study.flights.exception;

import java.util.Objects;

/**
 * Невозможный или некорректный маршрут.
 */
public class RouteException extends Exception {

    /** Причина отказа. */
    public enum Reason {
        /** Маршрут без сегментов. */
        EMPTY_ROUTE,
        /** Аэропорт прилёта предыдущего сегмента не совпадает с аэропортом вылета следующего. */
        BROKEN_CHAIN,
        /** Следующий рейс вылетает раньше прилёта предыдущего. */
        NEGATIVE_LAYOVER,
        /** Пересадка короче минимальной. */
        LAYOVER_TOO_SHORT,
        /** Не найден подходящий рейс-замена. */
        NO_REPLACEMENT
    }

    private final Reason reason;

    /**
     * @param message описание для человека
     */
    public RouteException(Reason reason, String message) {
        super(message);
        this.reason = Objects.requireNonNull(reason);
    }

    /** @return причина отказа */
    public Reason reason() {
        return reason;
    }
}