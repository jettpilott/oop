package ru.study.flights;

import ru.study.flights.booking.CabinClass;
import ru.study.flights.flight.Flight;
import ru.study.flights.flight.Segment;
import ru.study.flights.model.Airport;

import java.time.LocalDateTime;

final class TestData {
    static final Airport ARN = Airport.of("ARN", "Стокгольм", "Europe/Stockholm");
    static final Airport FRA = Airport.of("FRA", "Франкфурт", "Europe/Berlin");
    static final Airport DXB = Airport.of("DXB", "Дубай", "Asia/Dubai");
    static final Airport NRT = Airport.of("NRT", "Токио", "Asia/Tokyo");
    static final Airport HNL = Airport.of("HNL", "Гонолулу", "Pacific/Honolulu");
    static final Airport LAX = Airport.of("LAX", "Лос-Анджелес", "America/Los_Angeles");

    private TestData() { }

    static Flight flight(String no, Airport from, Airport to, String dep, String arr) {
        return new Flight(no, from, to, LocalDateTime.parse(dep), LocalDateTime.parse(arr), 1_000_000L);
    }

    static Segment eco(Flight f) {
        return new Segment(f, CabinClass.ECONOMY);
    }
}