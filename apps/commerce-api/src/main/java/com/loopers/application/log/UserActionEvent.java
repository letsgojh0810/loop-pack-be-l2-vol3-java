package com.loopers.application.log;

public record UserActionEvent(Long userId, String action, String target, Long targetId) {
}
