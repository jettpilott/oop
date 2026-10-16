package ru.study.flights.flight;

import java.time.Instant;
import java.util.Objects;

import ru.study.flights.booking.CabinClass;
import ru.study.flights.model.Airport;
/**
 * Сегмент маршрута: рейс, купленный в определённом классе обслуживания.
 *
 * @param flight рейс
 * @param cabin  класс обслуживания
 */
public record Segment(Flight flight, CabinClass cabin) implements Timed {

    /** Проверка на null. */
    public Segment {
        Objects.requireNonNull(flight, "flight");
        Objects.requireNonNull(cabin, "cabin");
    }

    /** @return аэропорт вылета */
    public Airport origin() { return flight.origin(); }

    /** @return аэропорт прилёта */
    public Airport destination() { return flight.destination(); }

    @Override
    public Instant departure() { return flight.departure(); }

    @Override
    public Instant arrival() { return flight.arrival(); }

    /** @return цена сегмента в копейках с учётом класса */
    public long priceKopecks() {
        return cabin.priceFor(flight.basePriceKopecks());
    }
}
