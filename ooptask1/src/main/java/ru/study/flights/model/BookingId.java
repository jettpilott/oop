package ru.study.flights.model;

/**
 * Идентификатор бронирования. Record даёт корректные equals/hashCode по значению.
 */
public record BookingId(String value) {

    public BookingId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Пустой номер бронирования");
        }
    }
}