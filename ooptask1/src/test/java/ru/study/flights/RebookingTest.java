package ru.study.flights;

import static org.junit.jupiter.api.Assertions.*;
import static ru.study.flights.TestData.*;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.study.flights.booking.Booking;
import ru.study.flights.booking.BookingStatus;
import ru.study.flights.booking.RebookingService;
import ru.study.flights.exception.RouteException;
import ru.study.flights.exception.RouteException.Reason;
import ru.study.flights.flight.Flight;
import ru.study.flights.flight.Route;
import ru.study.flights.model.BookingId;

class RebookingTest {
    private static final Duration MIN = Duration.ofMinutes(90);

    private final Flight xa101 = flight("XA101", ARN, FRA, "2026-11-10T07:00", "2026-11-10T09:00");
    private final Flight xa202 = flight("XA202", FRA, DXB, "2026-11-10T11:30", "2026-11-10T19:30");
    private final Flight xa303 = flight("XA303", DXB, NRT, "2026-11-10T21:00", "2026-11-11T11:30");
    private final Flight xb1 = flight("XB1", FRA, DXB, "2026-11-10T12:00", "2026-11-10T20:00"); // DXB: 60 мин
    private final Flight xb2 = flight("XB2", FRA, DXB, "2026-11-10T11:00", "2026-11-10T19:00"); // подходит
    private final Flight xb3 = flight("XB3", FRA, DXB, "2026-11-10T09:10", "2026-11-10T17:10"); // FRA: 10 мин
    private final Flight xb8 = flight("XB8", FRA, DXB, "2026-11-10T11:20", "2026-11-10T19:20"); // подходит, но позже

    private Route route() throws RouteException {
        return Route.of(List.of(eco(xa101), eco(xa202), eco(xa303)), MIN);
    }

    @Test
    void middleSegment_picksEarliestArrivalAmongValid() throws RouteException {
        Route result = RebookingService.replaceCancelled(route(), 1, List.of(xa202, xb1, xb3, xb8, xb2));
        assertEquals("XB2", result.segments().get(1).flight().number());
        assertEquals(route().segments().get(0), result.segments().get(0));
        assertEquals(route().segments().get(2), result.segments().get(2));
    }

    @Test
    void originalRouteIsNotModified() throws RouteException {
        Route original = route();
        RebookingService.replaceCancelled(original, 1, List.of(xb2));
        assertEquals("XA202", original.segments().get(1).flight().number());
    }

    @Test
    void onlyConnectionBreakingCandidates_throwsNoReplacement() throws RouteException {
        RouteException e = assertThrows(RouteException.class,
                () -> RebookingService.replaceCancelled(route(), 1, List.of(xb1, xb3)));
        assertEquals(Reason.NO_REPLACEMENT, e.reason());
    }

    @Test
    void cancelledFlightItselfIsNotAReplacement() {
        assertThrows(RouteException.class,
                () -> RebookingService.replaceCancelled(route(), 1, List.of(xa202)));
    }

    @Test
    void lastSegment_hasNoNextConstraint() throws RouteException {
        Flight alt = flight("XA304", DXB, NRT, "2026-11-10T22:00", "2026-11-11T12:30");
        Route result = RebookingService.replaceCancelled(route(), 2, List.of(alt));
        assertEquals("XA304", result.segments().get(2).flight().number());
    }

    @Test
    void invalidIndex_throwsIndexOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class,
                () -> RebookingService.replaceCancelled(route(), 5, List.of(xb2)));
    }

    @Test
    void booking_rebookChangesStatusAndRoute() throws RouteException {
        Booking b = new Booking(new BookingId("BK-1"), "Иванов", route());
        b.rebookCancelledSegment(1, List.of(xb2));
        assertEquals(BookingStatus.REBOOKED, b.status());
        assertEquals("XB2", b.route().segments().get(1).flight().number());
    }

    @Test
    void booking_failedRebookKeepsRoute_cancelledBookingRejectsOperations() throws RouteException {
        Booking b = new Booking(new BookingId("BK-2"), "Иванов", route());
        assertThrows(RouteException.class, () -> b.rebookCancelledSegment(1, List.of(xb1)));
        assertEquals(BookingStatus.CONFIRMED, b.status());
        assertEquals("XA202", b.route().segments().get(1).flight().number());
        b.cancel();
        assertThrows(IllegalStateException.class, b::cancel);
        assertThrows(IllegalStateException.class, () -> b.rebookCancelledSegment(1, List.of(xb2)));
    }
}