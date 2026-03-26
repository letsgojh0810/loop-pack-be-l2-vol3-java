package com.loopers.domain.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.ZonedDateTime;

@Entity
@Table(name = "event_handled")
public class EventHandled {

    @Id
    private String eventId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private ZonedDateTime handledAt;

    protected EventHandled() {}

    public static EventHandled of(String eventId, String eventType) {
        EventHandled e = new EventHandled();
        e.eventId = eventId;
        e.eventType = eventType;
        e.handledAt = ZonedDateTime.now();
        return e;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public ZonedDateTime getHandledAt() {
        return handledAt;
    }
}
