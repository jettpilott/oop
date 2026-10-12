package ru.study.flights.booking;

/** Класс обслуживания. Поля: название, коэффициент цены в процентах, норма багажа. */
public enum CabinClass {
    ECONOMY("Эконом", 100, 20),
    PREMIUM_ECONOMY("Комфорт", 160, 30),
    BUSINESS("Бизнес", 300, 40) {
        @Override
        public boolean hasLounge() { return true; }
    },
    FIRST("Первый", 500, 50) {
        @Override
        public boolean hasLounge() { return true; }
    };

    private final String title;
    private final int priceFactorPercent;
    private final int baggageKg;

    CabinClass(String title, int priceFactorPercent, int baggageKg) {
        this.title = title;
        this.priceFactorPercent = priceFactorPercent;
        this.baggageKg = baggageKg;
    }

    /** @return русское название класса */
    public String title() { return title; }

    /** @return норма бесплатного багажа, кг */
    public int baggageKg() { return baggageKg; }

    /** @return доступ в бизнес-зал; по умолчанию нет */
    public boolean hasLounge() { return false; }

    /**
     * Цена билета этого класса.
     *
     * @param baseKopecks базовая цена рейса в копейках
     * @return цена в копейках
     * @throws ArithmeticException при переполнении long
     */
    public long priceFor(long baseKopecks) {
        return Math.multiplyExact(baseKopecks, priceFactorPercent) / 100;
    }
}