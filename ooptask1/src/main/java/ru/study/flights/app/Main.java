package ru.study.flights.app;

import java.util.List;
import ru.study.flights.model.*;


import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ru.study.flights.flight.*;
import ru.study.flights.model.*;
import ru.study.flights.booking.*;
import ru.study.flights.view.RoutePrinter;
import ru.study.flights.exception.RouteException;


/** Консольная демонстрация: базовый и повышенный уровни. */
public final class Main {
    private static final Duration MIN_CONNECTION = Duration.ofMinutes(90);

    private static final Airport ARN = Airport.of("ARN", "Стокгольм", "Europe/Stockholm");
    private static final Airport FRA = Airport.of("FRA", "Франкфурт", "Europe/Berlin");
    private static final Airport DXB = Airport.of("DXB", "Дубай", "Asia/Dubai");
    private static final Airport NRT = Airport.of("NRT", "Токио", "Asia/Tokyo");
    private static final Airport HNL = Airport.of("HNL", "Гонолулу", "Pacific/Honolulu");
    private static final Airport LAX = Airport.of("LAX", "Лос-Анджелес", "America/Los_Angeles");
    private static final Airport SVO = Airport.of("SVO", "Москва", "Europe/Moscow");
    private static final Airport LED = Airport.of("LED", "Санкт-Петербург", "Europe/Moscow");
    private static final Airport JFK = Airport.of("JFK", "Нью-Йорк", "America/New_York");
    private static final Airport HEL = Airport.of("HEL", "Хельсинки", "Europe/Helsinki");
    private static final List<Airport> AIRPORTS = List.of(ARN, FRA, DXB, NRT, HNL, LAX, SVO, LED, JFK, HEL);

    private Main() { }

    public static void main(String[] args) {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        try {
            run();
        } catch (RouteException e) {
            System.out.println("Непредвиденный отказ [" + e.reason() + "]: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void run() throws RouteException {
        List<Flight> schedule = new ArrayList<>();
        List<Booking> bookings = new ArrayList<>();

        Flight xa101 = add(schedule, flight("XA101", ARN, FRA, "2026-11-10T07:00", "2026-11-10T09:00", 8_000));
        Flight xa202 = add(schedule, flight("XA202", FRA, DXB, "2026-11-10T11:30", "2026-11-10T19:30", 30_000));
        Flight xa303 = add(schedule, flight("XA303", DXB, NRT, "2026-11-10T21:00", "2026-11-11T11:30", 55_000));

        add(schedule, flight("XB1", FRA, DXB, "2026-11-10T12:00", "2026-11-10T20:00", 29_000));
        Flight xb2 = add(schedule, flight("XB2", FRA, DXB, "2026-11-10T11:00", "2026-11-10T19:00", 31_000));
        Flight xb3 = add(schedule, flight("XB3", FRA, DXB, "2026-11-10T09:10", "2026-11-10T17:10", 28_000));
        add(schedule, flight("XB4", FRA, DXB, "2026-11-10T08:00", "2026-11-10T16:00", 27_000));
        add(schedule, flight("XB8", FRA, DXB, "2026-11-10T11:20", "2026-11-10T19:20", 26_000));

        // ---------- БАЗОВЫЙ УРОВЕНЬ ----------
        section("1. Длительность, когда местное время прилёта «раньше» вылета");
        Flight xp10 = add(schedule, flight("XP10", NRT, HNL, "2026-06-10T18:00", "2026-06-10T06:00", 45_000));
        System.out.println("Токио 10.06 18:00 -> Гонолулу 10.06 06:00 (местное).");
        System.out.println("Фактически в пути: " + RoutePrinter.formatDuration(xp10.duration()));

        section("2. Сборка маршрута с проверкой минимальной стыковки (минимум 1ч 30мин)");
        Segment s1 = new Segment(xa101, CabinClass.ECONOMY);
        Segment s2 = new Segment(xa202, CabinClass.ECONOMY);
        Segment s3 = new Segment(xa303, CabinClass.BUSINESS);
        tryAssemble("ARN-FRA-DXB-NRT (пересадка в DXB ровно 1ч 30мин — допустимо):", s1, s2, s3);
        tryAssemble("ARN-FRA + XB3 (стыковка 10 минут):", s1, new Segment(xb3, CabinClass.ECONOMY));
        tryAssemble("ARN-FRA + XA303 (разрыв цепочки):", s1, s3);

        section("3. Суммарное время, пересадки, печать с пометкой смены даты");
        Route route = Route.of(List.of(s1, s2, s3), MIN_CONNECTION);
        System.out.print(RoutePrinter.format(route));

        section("4. Полиморфизм: duration() у рейса, сегмента и маршрута");
        for (Timed t : List.<Timed>of(xa101, s3, route)) {
            System.out.println("  " + RoutePrinter.describe(t));
        }

        // ---------- ПОВЫШЕННЫЙ УРОВЕНЬ ----------
        section("5. Ночь перехода на летнее время (Стокгольм, 29.03.2026)");
        Flight night = add(schedule, flight("XD500", DXB, ARN, "2026-03-28T23:00", "2026-03-29T03:00", 25_000));
        Flight usual = add(schedule, flight("XD500", DXB, ARN, "2026-03-21T23:00", "2026-03-22T03:00", 25_000));
        System.out.println("Расписание одно и то же: вылет 23:00, прилёт 03:00.");
        System.out.println("  обычная неделя:      " + RoutePrinter.formatDuration(usual.duration()));
        System.out.println("  в ночь перехода:     " + RoutePrinter.formatDuration(night.duration()));

        section("6. Отмена сегмента и поиск замены, сохраняющей стыковки");
        Booking booking = new Booking(new BookingId("BK-100"), "Петров П.", route);
        bookings.add(booking);
        System.out.println("До отмены:");
        System.out.print(RoutePrinter.format(booking.route()));
        System.out.println("Авиакомпания отменила XA202. Кандидаты XB1, XB3, XB4 ломают стыковки.");
        booking.rebookCancelledSegment(1, schedule);
        System.out.println("После (статус " + booking.status() + "), выбран "
                + booking.route().segments().get(1).flight().number() + ":");
        System.out.print(RoutePrinter.format(booking.route()));

        Booking second = new Booking(new BookingId("BK-101"), "Сидорова А.", route);
        bookings.add(second);
        System.out.println("Отмена XA303 (замен нет):");
        try {
            second.rebookCancelledSegment(2, schedule);
        } catch (RouteException e) {
            System.out.println("  [" + e.reason() + "] " + e.getMessage() + " -> предложим возврат билета");
        }

        section("7. Маршрут через линию перемены даты: Токио - Гонолулу - Лос-Анджелес");
        Flight xp20 = add(schedule, flight("XP20", HNL, LAX, "2026-06-10T09:00", "2026-06-10T17:30", 38_000));
        System.out.print(RoutePrinter.format(Route.of(List.of(
                        new Segment(xp10, CabinClass.ECONOMY), new Segment(xp20, CabinClass.PREMIUM_ECONOMY)),
                MIN_CONNECTION)));

        section("8. Наполнение модели");
        String[] passengers = {"Иванов И.", "Смирнова О.", "Кузнецов Д.", "Попова Е.", "Орлов С."};
        for (int i = 0; i < passengers.length; i++) {
            Flight shuttle = add(schedule, flight("SL" + (i + 1), SVO, LED,
                    LocalDate.of(2026, 11, 10).plusDays(i) + "T08:00",
                    LocalDate.of(2026, 11, 10).plusDays(i) + "T09:30", 3_500));
            bookings.add(new Booking(new BookingId("BK-%03d".formatted(i + 1)), passengers[i],
                    Route.of(List.of(new Segment(shuttle, CabinClass.ECONOMY)), MIN_CONNECTION)));
        }
        add(schedule, flight("SL6", LED, HEL, "2026-11-12T10:00", "2026-11-12T11:00", 9_000));
        add(schedule, flight("SL7", HEL, JFK, "2026-11-12T13:00", "2026-11-12T15:30", 60_000));
        System.out.println("Аэропортов: " + AIRPORTS.size() + ", рейсов: " + schedule.size()
                + ", броней: " + bookings.size() + ". Всего объектов: "
                + (AIRPORTS.size() + schedule.size() + bookings.size()));
        System.out.println("Использован XB2: " + xb2.number() + " (цена " + Money.format(xb2.basePriceKopecks()) + ")");
    }

    private static Flight flight(String no, Airport from, Airport to, String dep, String arr, long priceRub) {
        return new Flight(no, from, to, LocalDateTime.parse(dep), LocalDateTime.parse(arr), priceRub * 100);
    }

    private static Flight add(List<Flight> schedule, Flight f) {
        schedule.add(f);
        return f;
    }

    private static void tryAssemble(String title, Segment... segments) {
        System.out.println(title);
        try {
            System.out.print(RoutePrinter.format(Route.of(List.of(segments), MIN_CONNECTION)));
        } catch (RouteException e) {
            System.out.println("  ОТКЛОНЕНО [" + e.reason() + "]: " + e.getMessage());
        }
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }
}