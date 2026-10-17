package ru.study.flights;

import static org.junit.jupiter.api.Assertions.*;
import static ru.study.flights.TestData.*;

import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import ru.study.flights.flight.Flight;

class FlightTest {

    @Test
    void dateLine_localArrivalEarlierButDurationPositive() {
        Flight f = flight("XP10", NRT, HNL, "2026-06-10T18:00", "2026-06-10T06:00");
        assertTrue(f.arrivalLocal().isBefore(f.departureLocal()));
        assertEquals(Duration.ofHours(7), f.duration());
    }

    @Test
    void overnightFlight_nextDayAndDuration() {
        Flight f = flight("XA303", DXB, NRT, "2026-11-10T21:00", "2026-11-11T11:30");
        assertEquals(Duration.ofMinutes(9 * 60 + 30), f.duration());
        assertEquals(1, f.arrivalDayShift());
    }

    @Test
    void dstNight_sameLocalScheduleGivesShorterFlight() {
        Flight night = flight("XD500", DXB, ARN, "2026-03-28T23:00", "2026-03-29T03:00");
        Flight usual = flight("XD500", DXB, ARN, "2026-03-21T23:00", "2026-03-22T03:00");
        assertEquals(usual.departureLocal().toLocalTime(), night.departureLocal().toLocalTime());
        assertEquals(usual.arrivalLocal().toLocalTime(), night.arrivalLocal().toLocalTime());
        assertEquals(Duration.ofHours(7), usual.duration());
        assertEquals(Duration.ofHours(6), night.duration());
    }

    @Test
    void arrivalInDstGap_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> flight("XD1", DXB, ARN, "2026-03-28T23:00", "2026-03-29T02:30"));
    }

    @Test
    void arrivalBeforeDepartureInAbsoluteTime_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> flight("X", ARN, FRA, "2026-11-10T10:00", "2026-11-10T08:30"));
    }

    @Test
    void tooLongFlight_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> flight("X", ARN, NRT, "2026-11-10T10:00", "2026-11-12T10:00"));
    }

    @Test
    void sameAirportAndNegativePrice_throw() {
        LocalDateTime dep = LocalDateTime.parse("2026-11-10T10:00");
        LocalDateTime arr = LocalDateTime.parse("2026-11-10T12:00");
        assertThrows(IllegalArgumentException.class, () -> new Flight("X", ARN, ARN, dep, arr, 1));
        assertThrows(IllegalArgumentException.class, () -> new Flight("X", ARN, FRA, dep, arr, -1));
        assertThrows(NullPointerException.class, () -> new Flight("X", null, FRA, dep, arr, 1));
    }
}