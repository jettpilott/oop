package ru.study.flights.model;

/** Форматирование денег, хранимых целыми копейками. */
public final class Money {
    private Money() { }

    /**
     * @param kopecks сумма в копейках
     * возвращает строку вида {@code 1234,56 руб.}
     */
    public static String format(long kopecks) {
        String sign = kopecks < 0 ? "-" : "";
        long abs = Math.abs(kopecks);
        return "%s%d,%02d руб.".formatted(sign, abs / 100, abs % 100);
    }
}