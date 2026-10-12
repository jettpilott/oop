package ru.study.flights.flight;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import ru.study.flights.model.*;

/**
 * класс 'рейс'. расписание хранится в местном времени аэропортов, вся арифметика идёт через {@link Instant}.
 *
 * @param number            номер рейса
 * @param origin            аэропорт вылета
 * @param destination       аэропорт прилёта
 * @param departureLocal    местное время вылета (по поясу origin)
 * @param arrivalLocal      местное время прилёта (по поясу destination)
 * @param basePriceKopecks  базовая цена в копейках
 */
public record Flight(String number, Airport origin, Airport destination,
                     LocalDateTime departureLocal, LocalDateTime arrivalLocal,
                     long basePriceKopecks) implements Timed {

    /** Максимально допустимая длительность рейса. */
    public static final Duration MAX_DURATION = Duration.ofHours(24);

    /**
     * Проверка инвариантов рейса.
     *
     * @throws IllegalArgumentException при пустом номере, совпадающих аэропортах, отрицательной цене,
     *                                  несуществующем местном времени, неположительной или
     *                                  чрезмерной длительности
     */
    public Flight {
        Objects.requireNonNull(number, "number");
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(departureLocal, "departureLocal");
        Objects.requireNonNull(arrivalLocal, "arrivalLocal");
        if (number.isBlank()) {
            throw new IllegalArgumentException("Номер рейса пуст");
        }
        if (origin.equals(destination)) {
            throw new IllegalArgumentException("Аэропорты вылета и прилёта совпадают: " + origin);
        }
        if (basePriceKopecks < 0) {
            throw new IllegalArgumentException("Отрицательная цена: " + basePriceKopecks);
        }
        Duration d = Duration.between(toInstant(departureLocal, origin), toInstant(arrivalLocal, destination));
        if (d.isNegative() || d.isZero()) {
            throw new IllegalArgumentException(
                    "Рейс %s прилетает не позже вылета (по абсолютному времени)".formatted(number));
        }
        if (d.compareTo(MAX_DURATION) > 0) {
            throw new IllegalArgumentException(
                    "Рейс %s длится %d ч — больше допустимых %d ч"
                            .formatted(number, d.toHours(), MAX_DURATION.toHours()));
        }
    }

    /** @return момент вылета */
    @Override
    public Instant departure() {
        return toInstant(departureLocal, origin);
    }

    /** @return момент прилёта */
    @Override
    public Instant arrival() {
        return toInstant(arrivalLocal, destination);
    }

    private static Instant toInstant(LocalDateTime local, Airport airport) {
        if (airport.zone().getRules().getValidOffsets(local).isEmpty()) {
            throw new IllegalArgumentException(
                    "Время %s не существует в поясе %s (переход на летнее время)"
                            .formatted(local, airport.zone()));
        }
        return local.atZone(airport.zone()).toInstant();
    }

    /**
     * @return сдвиг календарной даты прилёта относительно даты вылета (по местным датам):
     *         0 — та же дата, 1 — следующая, -1 — предыдущая (линия перемены даты)
     */
    public int arrivalDayShift() {
        return (int) ChronoUnit.DAYS.between(departureLocal.toLocalDate(), arrivalLocal.toLocalDate());
    }
}