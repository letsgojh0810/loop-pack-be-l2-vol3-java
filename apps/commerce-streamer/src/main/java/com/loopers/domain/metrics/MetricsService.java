package com.loopers.domain.metrics;

import com.loopers.domain.idempotency.EventHandled;
import com.loopers.domain.idempotency.EventHandledRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class MetricsService {

    private final ProductMetricsRepository productMetricsRepository;
    private final EventHandledRepository eventHandledRepository;

    public boolean isHandled(String eventId) {
        return eventHandledRepository.existsById(eventId);
    }

    public void markHandled(String eventId, String eventType) {
        eventHandledRepository.save(EventHandled.of(eventId, eventType));
    }

    public void upsertLike(Long productId, long version, int delta) {
        ProductMetrics metrics = productMetricsRepository.findById(productId)
                .orElseGet(() -> ProductMetrics.create(productId));
        if (version > metrics.getLastEventVersion()) {
            metrics.incrementLike(delta, version);
            productMetricsRepository.save(metrics);
        }
    }

    public void upsertView(Long productId, long version) {
        ProductMetrics metrics = productMetricsRepository.findById(productId)
                .orElseGet(() -> ProductMetrics.create(productId));
        if (version > metrics.getLastEventVersion()) {
            metrics.incrementView(version);
            productMetricsRepository.save(metrics);
        }
    }

    public void upsertSales(Long productId, long version, int quantity) {
        ProductMetrics metrics = productMetricsRepository.findById(productId)
                .orElseGet(() -> ProductMetrics.create(productId));
        if (version > metrics.getLastEventVersion()) {
            metrics.incrementSales(quantity, version);
            productMetricsRepository.save(metrics);
        }
    }
}
