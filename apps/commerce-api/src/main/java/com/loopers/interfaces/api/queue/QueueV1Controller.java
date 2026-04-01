package com.loopers.interfaces.api.queue;

import com.loopers.application.queue.QueueFacade;
import com.loopers.application.queue.QueueInfo;
import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/queue")
public class QueueV1Controller implements QueueV1ApiSpec {

    private static final String HEADER_LOGIN_ID = "X-Loopers-LoginId";
    private static final String HEADER_LOGIN_PW = "X-Loopers-LoginPw";

    private final UserFacade userFacade;
    private final QueueFacade queueFacade;

    @PostMapping("/enter")
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<QueueV1Dto.EnterResponse> enter(
        @RequestHeader(HEADER_LOGIN_ID) String loginId,
        @RequestHeader(HEADER_LOGIN_PW) String password
    ) {
        UserInfo currentUser = userFacade.getMe(loginId, password);
        QueueInfo info = queueFacade.enter(currentUser.id());
        return ApiResponse.success(QueueV1Dto.EnterResponse.from(info));
    }

    @GetMapping("/position")
    @Override
    public ApiResponse<QueueV1Dto.PositionResponse> getPosition(
        @RequestHeader(HEADER_LOGIN_ID) String loginId,
        @RequestHeader(HEADER_LOGIN_PW) String password
    ) {
        UserInfo currentUser = userFacade.getMe(loginId, password);
        QueueInfo info = queueFacade.getPosition(currentUser.id());
        return ApiResponse.success(QueueV1Dto.PositionResponse.from(info));
    }
}
