package ru.study.flights;

import static org.junit.jupiter.api.Assertions.*;
import static ru.study.flights.TestData.*;

import java.time.Duration;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import ru.study.flights.booking.Booking;
import ru.study.flights.booking.CabinClass;
import ru.study.flights.exception.RouteException;
import ru.study.flights.flight.Route;
import ru.study.flights.model.Airport;
import ru.study.flights.model.BookingId;
import ru.study.flights.model.Money;

class ContractTest {

    @Test
    void airport_equalsHashCodeContract() {
        Airport a = new Airport("ARN", "Стокгольм", ZoneId.of("Europe/Stockholm"));
        Airport b = new Airport("ARN", "Arlanda", ZoneId.of("Europe/Stockholm"));
        Airport c = FRA;
        assertEquals(a, a);                           // рефлексивность
        assertEquals(a, b);
        assertEquals(b, a);                           // симметричность
        assertEquals(a.hashCode(), b.hashCode());     // согласованность с equals
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertNotEquals(a, "ARN");
        assertEquals(1, new HashSet<>(List.of(a, b)).size());
    }

    @Test
    void airport_invalidCode_throws() {
        assertThrows(IllegalArgumentException.class, () -> Airport.of("arn", "x", "Europe/Stockholm"));
        assertThrows(IllegalArgumentException.class, () -> Airport.of("ARNN", "x", "Europe/Stockholm"));
    }

    @Test
    void bookingId_recordEqualsAndValidation() {
        assertEquals(new BookingId("BK-1"), new BookingId("BK-1"));
        assertEquals(new BookingId("BK-1").hashCode(), new BookingId("BK-1").hashCode());
        assertNotEquals(new BookingId("BK-1"), new BookingId("BK-2"));
        assertThrows(IllegalArgumentException.class, () -> new BookingId(" "));
    }

    @Test
    void booking_equalityByIdOnly() throws RouteException {
        Route r = Route.of(List.of(eco(flight("A", ARN, FRA, "2026-11-10T07:00", "2026-11-10T09:00"))),
                Duration.ofMinutes(90));
        Booking x = new Booking(new BookingId("BK-1"), "Иванов", r);
        Booking y = new Booking(new BookingId("BK-1"), "Петров", r);
        assertEquals(x, y);
        assertEquals(x.hashCode(), y.hashCode());
        Set<Booking> set = new HashSet<>(List.of(x));
        assertTrue(set.contains(y));
    }

    @Test
    void cabinClass_fieldsAndConstantBodies() {
        assertEquals(10_000L, CabinClass.ECONOMY.priceFor(10_000L));
        assertEquals(30_000L, CabinClass.BUSINESS.priceFor(10_000L));
        assertTrue(CabinClass.BUSINESS.hasLounge());
        assertFalse(CabinClass.ECONOMY.hasLounge());
        assertThrows(ArithmeticException.class, () -> CabinClass.FIRST.priceFor(Long.MAX_VALUE));
    }

    @Test
    void money_formatsKopecks() {
        assertEquals("1234,56 руб.", Money.format(123_456));
        assertEquals("0,05 руб.", Money.format(5));
    }
}