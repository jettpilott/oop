package ru.study.flights.booking;



import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

import ru.study.flights.exception.*;

import ru.study.flights.exception.RouteException.Reason;
import ru.study.flights.flight.*;

/** поиск замены отменённого сегмента с сохранением  стыковок. */
public final class RebookingService {
    private static final Comparator<Route> BEST =
            Comparator.comparing(Route::arrival).thenComparingLong(Route::totalPriceKopecks);

    private RebookingService() { }

    /**
     * Ищет рейс, который заменит отменённый сегмент. Кандидат принимается, только если между теми же
     * аэропортами и весь получившийся маршрут проходит проверку стыковок (в т. ч. со следующим сегментом).
     * Из подходящих выбирается тот, что приводит к самому раннему прибытию, при равенстве — самый дешёвый.
     *
     * @param route    исходный маршрут
     * @param index    индекс отменённого сегмента
     * @param schedule доступные рейсы
     * @return новый маршрут с заменой
     * @throws RouteException с причиной {@link Reason#NO_REPLACEMENT}, если замены нет
     */
    public static Route replaceCancelled(Route route, int index, Collection<Flight> schedule)
            throws RouteException {
        Objects.requireNonNull(route, "route");
        Objects.requireNonNull(schedule, "schedule");
        Objects.checkIndex(index, route.segments().size());
        Segment cancelled = route.segments().get(index);

        Route best = null;
        int rejected = 0;
        for (Flight candidate : schedule) {
            boolean sameLeg = candidate.origin().equals(cancelled.origin())
                    && candidate.destination().equals(cancelled.destination());
            if (!sameLeg || candidate.equals(cancelled.flight())) {
                continue;
            }
            try {
                Route variant = route.withReplacement(index, new Segment(candidate, cancelled.cabin()));
                if (best == null || BEST.compare(variant, best) < 0) {
                    best = variant;
                }
            } catch (RouteException e) {
                rejected++;
            }
        }
        if (best == null) {
            throw new RouteException(Reason.NO_REPLACEMENT,
                    "Замены для %s нет (отклонено из-за стыковок: %d)"
                            .formatted(cancelled.flight().number(), rejected));
        }
        return best;
    }
}