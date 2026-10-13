package ru.study.flights.flight;

import java.time.Duration;
import java.util.List;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ru.study.flights.exception.RouteException.Reason;
import ru.study.flights.exception.RouteException;

/**
 * Неизменяемый маршрут из одного или нескольких сегментов с проверенными стыковками.
 */
public final class Route implements Timed {
    private final List<Segment> segments;
    private final Duration minConnection;

    private Route(List<Segment> segments, Duration minConnection) {
        this.segments = segments;
        this.minConnection = minConnection;
    }

    /**
     * Собирает маршрут и проверяет стыковки.
     *
     * @param segments      сегменты по порядку (список копируется)
     * @param minConnection минимальное время пересадки (допустимо ровно такое же)
     * @return маршрут
     * @throws RouteException если маршрут пуст, цепочка аэропортов разорвана или пересадка слишком короткая
     */
    public static Route of(List<Segment> segments, Duration minConnection) throws RouteException {
        Objects.requireNonNull(segments, "segments");
        Objects.requireNonNull(minConnection, "minConnection");
        if (minConnection.isNegative()) {
            throw new IllegalArgumentException("Минимальная пересадка отрицательна");
        }
        if (segments.isEmpty()) {
            throw new RouteException(Reason.EMPTY_ROUTE, "Маршрут не содержит сегментов");
        }
        List<Segment> copy = List.copyOf(segments);
        for (int i = 1; i < copy.size(); i++) {
            Segment prev = copy.get(i - 1);
            Segment next = copy.get(i);
            if (!prev.destination().equals(next.origin())) {
                throw new RouteException(Reason.BROKEN_CHAIN,
                        "%s прилетает в %s, а %s вылетает из %s"
                                .formatted(prev.flight().number(), prev.destination(),
                                        next.flight().number(), next.origin()));
            }
            Duration layover = Duration.between(prev.arrival(), next.departure());
            if (layover.isNegative()) {
                throw new RouteException(Reason.NEGATIVE_LAYOVER,
                        "%s вылетает раньше, чем прилетает %s"
                                .formatted(next.flight().number(), prev.flight().number()));
            }
        }
        return new Route(copy, minConnection);
    }

    /** @return неизменяемый список сегментов */
    public List<Segment> segments() { return segments; }

    /** @return минимальное время пересадки, с которым собран маршрут */
    public Duration minConnection() { return minConnection; }

    @Override
    public Instant departure() { return segments.get(0).departure(); }

    @Override
    public Instant arrival() { return segments.get(segments.size() - 1).arrival(); }

    /** @return длительности пересадок по порядку (на одну меньше, чем сегментов) */
    public List<Duration> layovers() {
        List<Duration> result = new ArrayList<>();
        for (int i = 1; i < segments.size(); i++) {
            result.add(Duration.between(segments.get(i - 1).arrival(), segments.get(i).departure()));
        }
        return List.copyOf(result);
    }

    /** @return суммарное время на пересадках */
    public Duration totalLayover() {
        return layovers().stream().reduce(Duration.ZERO, Duration::plus);
    }

    /** @return суммарное время в воздухе */
    public Duration totalFlightTime() {
        return segments.stream().map(Segment::duration).reduce(Duration.ZERO, Duration::plus);
    }

    /** @return полная стоимость в копейках */
    public long totalPriceKopecks() {
        return segments.stream().mapToLong(Segment::priceKopecks).sum();
    }

    /**
     * Возвращает новый маршрут с заменённым сегментом; исходный не меняется.
     *
     * @param index       индекс заменяемого сегмента
     * @param replacement новый сегмент между теми же аэропортами
     * @return новый маршрут
     * @throws RouteException если замена нарушает стыковки или меняет аэропорты
     */
    public Route withReplacement(int index, Segment replacement) throws RouteException {
        Objects.checkIndex(index, segments.size());
        Objects.requireNonNull(replacement, "replacement");
        Segment old = segments.get(index);
        if (!old.origin().equals(replacement.origin()) || !old.destination().equals(replacement.destination())) {
            throw new RouteException(Reason.BROKEN_CHAIN,
                    "Замена %s не соединяет %s и %s".formatted(replacement.flight().number(),
                            old.origin(), old.destination()));
        }
        List<Segment> copy = new ArrayList<>(segments);
        copy.set(index, replacement);
        return of(copy, minConnection);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof Route r && segments.equals(r.segments) && minConnection.equals(r.minConnection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(segments, minConnection);
    }
}