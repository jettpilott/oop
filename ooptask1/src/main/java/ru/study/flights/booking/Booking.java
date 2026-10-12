package ru.study.flights.booking;

import ru.study.flights.flight.Route;
import ru.study.flights.model.BookingId;

import java.util.Objects;

/** Бронирование маршрута пассажиром. Равенство по идентификатору. */
public final class Booking {
    private final BookingId id;
    private final String passenger;
    private Route route;
    private BookingStatus status = BookingStatus.CONFIRMED;

    /**
     * @param id        номер брони
     * @param passenger имя пассажира
     * @param route     маршрут (неизменяемый, копия не нужна)
     */
    public Booking(BookingId id, String passenger, Route route) {
        this.id = Objects.requireNonNull(id, "id");
        this.passenger = passenger;
        this.route = Objects.requireNonNull(route, "route");
    }

    /** @return номер брони */
    public BookingId id() { return id; }

    /** @return имя пассажира */
    public String passenger() { return passenger; }

    /** @return текущий маршрут */
    public Route route() { return route; }

    /** @return статус */
    public BookingStatus status() { return status; }

    /**
     * Отменяет бронь.
     */
    public void cancel() {
        status = BookingStatus.CANCELLED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof Booking b && id.equals(b.id);
    }

}