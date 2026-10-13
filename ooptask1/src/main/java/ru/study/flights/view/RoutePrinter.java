package ru.study.flights.view;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

import ru.study.flights.flight.*;
import ru.study.flights.model.*;
import ru.study.flights.booking.*;


/** Текстовое представление маршрутов. */
public final class RoutePrinter {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM HH:mm");

    private RoutePrinter() { }

    /**
     * @param d длительность
     * @return строка вида {@code 9ч 30мин}
     */
    public static String formatDuration(Duration d) {
        return "%dч %02dмин".formatted(d.toHours(), d.toMinutesPart());
    }


    /**
     * Печатает маршрут с местным временем, пометкой смены даты и пересадками.
     *
     * @param route маршрут
     */
    public static String format(Route route) {
        StringBuilder sb = new StringBuilder();
        List<Segment> segs = route.segments();
        List<Duration> layovers = route.layovers();
        for (int i = 0; i < segs.size(); i++) {
            Segment s = segs.get(i);
            Flight f = s.flight();
            sb.append("  %d) %s  %s %s -> %s %s%s | в пути %s | %s%n".formatted(
                    i + 1, f.number(),
                    f.origin(), FMT.format(f.departureLocal()),
                    f.destination(), FMT.format(f.arrivalLocal()), mark(f),
                    formatDuration(f.duration()), s.cabin().title()));
            if (i < layovers.size()) {
                sb.append("     пересадка в %s: %s%n".formatted(f.destination(), formatDuration(layovers.get(i))));
            }
        }
        sb.append("  Итого: %s (в воздухе %s, на пересадках %s), %s%n".formatted(
                formatDuration(route.duration()), formatDuration(route.totalFlightTime()),
                formatDuration(route.totalLayover()), Money.format(route.totalPriceKopecks())));
        return sb.toString();
    }

    private static String mark(Flight f) {
        String mark = switch (f.arrivalDayShift()) {
            case 0 -> "";
            default -> " (%+d д.)".formatted(f.arrivalDayShift());
        };
        if (f.arrivalLocal().isBefore(f.departureLocal())) {
            mark += " [местное время прилёта раньше вылета]";
        }
        return mark;
    }
}