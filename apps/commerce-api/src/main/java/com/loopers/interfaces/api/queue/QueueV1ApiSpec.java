package com.loopers.interfaces.api.queue;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Queue", description = "주문 대기열 API")
public interface QueueV1ApiSpec {

    @Operation(summary = "대기열 진입", description = "주문 대기열에 진입하고 현재 순번을 반환합니다.")
    ApiResponse<QueueV1Dto.EnterResponse> enter(
        @Parameter(description = "로그인 ID", required = true) String loginId,
        @Parameter(description = "비밀번호", required = true) String password
    );

    @Operation(summary = "순번 조회", description = "현재 순번과 예상 대기 시간을 조회합니다. 순번이 null이면 입장 토큰이 발급된 상태입니다.")
    ApiResponse<QueueV1Dto.PositionResponse> getPosition(
        @Parameter(description = "로그인 ID", required = true) String loginId,
        @Parameter(description = "비밀번호", required = true) String password
    );
}
