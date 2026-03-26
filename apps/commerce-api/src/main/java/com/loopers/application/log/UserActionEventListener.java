package com.loopers.application.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserActionEventListener {

    @EventListener
    public void onUserAction(UserActionEvent event) {
        log.info("[유저행동로그] action={}, target={}, targetId={}, userId={}", event.action(), event.target(), event.targetId(), event.userId());
    }
}
