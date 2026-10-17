package ru.study.flights.booking;



import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

import ru.study.flights.exception.*;


import ru.study.flights.exception.RouteException;
import ru.study.flights.exception.RouteException.Reason;

import ru.study.flights.exception.RouteException.Reason;
import ru.study.flights.flight.*;

/** поиск замены отменённого сегмента с сохранением  стыковок. */
public final class RebookingService {
    /** Лучший кандидат: самый ранний прилёт заменяющего сегмента, при равенстве — самый дешёвый. */
    private static final Comparator<Segment> BEST_SEGMENT =
            Comparator.comparing(Segment::arrival).thenComparingLong(Segment::priceKopecks);

    private RebookingService() { }

    /**
     * Ищет рейс, который заменит отменённый сегмент. Кандидат принимается, только если он соединяет
     * те же аэропорты и весь получившийся маршрут проходит проверку стыковок (в том числе со следующим
     * сегментом). Из подходящих выбирается рейс с самым ранним прилётом, при равенстве — самый дешёвый.
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

        Route bestRoute = null;
        Segment bestSegment = null;
        int rejected = 0;
        for (Flight candidate : schedule) {
            boolean sameLeg = candidate.origin().equals(cancelled.origin())
                    && candidate.destination().equals(cancelled.destination());
            if (!sameLeg || candidate.equals(cancelled.flight())) {
                continue;
            }
            Segment seg = new Segment(candidate, cancelled.cabin());
            try {
                Route variant = route.withReplacement(index, seg);
                if (bestSegment == null || BEST_SEGMENT.compare(seg, bestSegment) < 0) {
                    bestRoute = variant;
                    bestSegment = seg;
                }
            } catch (RouteException e) {
                rejected++;   // кандидат ломает стыковку — считаем и идём дальше
            }
        }
        if (bestRoute == null) {
            throw new RouteException(Reason.NO_REPLACEMENT,
                    "Замены для %s нет (отклонено из-за стыковок: %d)"
                            .formatted(cancelled.flight().number(), rejected));
        }
        return bestRoute;
    }
}