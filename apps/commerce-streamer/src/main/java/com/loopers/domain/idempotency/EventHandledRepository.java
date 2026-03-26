package com.loopers.domain.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventHandledRepository extends JpaRepository<EventHandled, String> {
}
