package ru.study.flights;

import static org.junit.jupiter.api.Assertions.*;
import static ru.study.flights.TestData.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.study.flights.booking.CabinClass;
import ru.study.flights.exception.RouteException;
import ru.study.flights.exception.RouteException.Reason;
import ru.study.flights.flight.Flight;
import ru.study.flights.flight.Route;
import ru.study.flights.flight.Segment;
import ru.study.flights.model.Money;

class RouteTest {
    private static final Duration MIN = Duration.ofMinutes(90);

    private final Flight xa101 = flight("XA101", ARN, FRA, "2026-11-10T07:00", "2026-11-10T09:00");
    private final Flight xa202 = flight("XA202", FRA, DXB, "2026-11-10T11:30", "2026-11-10T19:30");
    private final Flight xa303 = flight("XA303", DXB, NRT, "2026-11-10T21:00", "2026-11-11T11:30");

    @Test
    void totals_flightTimePlusLayoversEqualsTotal() throws RouteException {
        Route r = Route.of(List.of(eco(xa101), eco(xa202), eco(xa303)), MIN);
        assertEquals(Duration.ofMinutes(20 * 60 + 30), r.duration());
        assertEquals(Duration.ofMinutes(16 * 60 + 30), r.totalFlightTime());
        assertEquals(Duration.ofHours(4), r.totalLayover());
        assertEquals(r.duration(), r.totalFlightTime().plus(r.totalLayover()));
        assertEquals(List.of(Duration.ofMinutes(150), Duration.ofMinutes(90)), r.layovers());
    }

    @Test
    void layoverExactlyMinimum_isAllowed() throws RouteException {
        Flight next = flight("N", FRA, DXB, "2026-11-10T10:30", "2026-11-10T18:30"); // ровно 90 мин
        assertDoesNotThrow(() -> Route.of(List.of(eco(xa101), eco(next)), MIN));
        assertEquals(Duration.ofMinutes(90), Route.of(List.of(eco(xa101), eco(next)), MIN).layovers().get(0));
    }

    @Test
    void layoverOneMinuteBelowMinimum_throws() {
        Flight next = flight("N", FRA, DXB, "2026-11-10T10:29", "2026-11-10T18:29"); // 89 мин
        RouteException e = assertThrows(RouteException.class, () -> Route.of(List.of(eco(xa101), eco(next)), MIN));
        assertEquals(Reason.LAYOVER_TOO_SHORT, e.reason());
    }

    @Test
    void nextFlightDepartsBeforeArrival_throws() {
        Flight next = flight("N", FRA, DXB, "2026-11-10T08:30", "2026-11-10T16:30");
        RouteException e = assertThrows(RouteException.class, () -> Route.of(List.of(eco(xa101), eco(next)), MIN));
        assertEquals(Reason.NEGATIVE_LAYOVER, e.reason());
    }

    @Test
    void brokenChain_throws() {
        RouteException e = assertThrows(RouteException.class, () -> Route.of(List.of(eco(xa101), eco(xa303)), MIN));
        assertEquals(Reason.BROKEN_CHAIN, e.reason());
    }

    @Test
    void emptyRoute_throws() {
        RouteException e = assertThrows(RouteException.class, () -> Route.of(List.of(), MIN));
        assertEquals(Reason.EMPTY_ROUTE, e.reason());
    }

    @Test
    void routeAcrossDateLine_correctTotals() throws RouteException {
        Flight a = flight("XP10", NRT, HNL, "2026-06-10T18:00", "2026-06-10T06:00");
        Flight b = flight("XP20", HNL, LAX, "2026-06-10T09:00", "2026-06-10T17:30");
        Route r = Route.of(List.of(eco(a), eco(b)), MIN);
        assertEquals(Duration.ofHours(3), r.totalLayover());
        assertEquals(Duration.ofMinutes(12 * 60 + 30), r.totalFlightTime());
        assertEquals(Duration.ofMinutes(15 * 60 + 30), r.duration());
    }

    @Test
    void segmentsAreProtectedFromOutsideChanges() throws RouteException {
        List<Segment> source = new ArrayList<>(List.of(eco(xa101)));
        Route r = Route.of(source, MIN);
        source.add(eco(xa202));                      // изменение исходного списка не влияет
        assertEquals(1, r.segments().size());
        assertThrows(UnsupportedOperationException.class, () -> r.segments().add(eco(xa202)));
    }

    @Test
    void priceIsInKopecksWithCabinFactor() throws RouteException {
        Route r = Route.of(List.of(new Segment(xa101, CabinClass.BUSINESS)), MIN);
        assertEquals(3_000_000L, r.totalPriceKopecks());
        assertEquals("30000,00 руб.", Money.format(r.totalPriceKopecks()));
    }
}