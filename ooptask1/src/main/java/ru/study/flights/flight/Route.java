package ru.study.flights.flight;

import java.time.Duration;
import java.util.List;

/**
 * Неизменяемый маршрут из одного или нескольких сегментов с проверенными стыковками.
 */
public final class Route {
    private final List<Segment> segments;
    private final Duration minConnection;

    private Route(List<Segment> segments, Duration minConnection) {
        this.segments = segments;
        this.minConnection = minConnection;
    }

}