package ru.study.flights.app;

import java.util.List;
import ru.study.flights.model.*;

public final class Main {

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


    public static void main(String[] args) {

            System.out.println("Hello");

    }
}