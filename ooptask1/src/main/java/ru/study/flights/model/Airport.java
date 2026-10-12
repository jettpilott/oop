package ru.study.flights.model;

import java.time.ZoneId;
import java.util.Objects;

/**
 * класс 'аэропорт'. идентификатор — трёхбуквенный код IATA. два аэропорта с одним кодом
 * считаются одним и тем же объектом, остальные поля в равенстве не участвуют.
 */
public final class Airport {
    private final String code;
    private final String city;
    private final ZoneId zone;

    /**
     * @param code код IATA (три заглавные латинские буквы)
     * @param city название города
     * @param zone часовой пояс аэропорта
     * @throws IllegalArgumentException если код имеет неверный формат
     */
    public Airport(String code, String city, ZoneId zone) {
        Objects.requireNonNull(code, "code");
        if (!code.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Код IATA — три заглавные латинские буквы: " + code);
        }
        this.code = code;
        this.city = Objects.requireNonNull(city, "city");
        this.zone = Objects.requireNonNull(zone, "zone");
    }

    /**
     * Удобная фабрика, принимающая пояс строкой, например {@code "Europe/Stockholm"}.
     *
     * @param code   код IATA
     * @param city   город
     * @param zoneId идентификатор часового пояса
     * @return новый аэропорт
     */
    public static Airport of(String code, String city, String zoneId) {
        return new Airport(code, city, ZoneId.of(zoneId));
    }

    /** @return код IATA */
    public String code() { return code; }

    /** @return город */
    public String city() { return city; }

    /** @return часовой пояс */
    public ZoneId zone() { return zone; }

    /** Равенство только по коду IATA. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof Airport other && code.equals(other.code);
    }

    @Override
    public String toString() {
        return code;
    }
}