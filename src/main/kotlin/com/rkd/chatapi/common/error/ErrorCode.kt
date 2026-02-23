package com.rkd.chatapi.common.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val code: String,
    val message: String,
    val httpStatus: HttpStatus
) {
    // common
    INTERNAL_SERVER_ERROR("GLB0001", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCESS_DENIED("GLB0002", "Access denied", HttpStatus.UNAUTHORIZED),
    INPUT_INVALID_VALUE("GLB0003", "Invalid input value", HttpStatus.BAD_REQUEST),

    // auth
    INVALID_API_KEY("AUTH0001", "유효하지 않은 API Key 입니다.", HttpStatus.BAD_REQUEST),
    API_KEY_NOT_EXIST("AUTH0002", "API Key가 존재하지 않습니다.", HttpStatus.BAD_REQUEST),

    // user
    USER_ALREADY_EXIST("USER0001", "이미 등록된 유저입니다.", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("USER0002", "유저를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),

    // jwt
    INVALID_ACCESS_TOKEN("JWT0001", "유효하지 않은 액세스 토큰입니다.", HttpStatus.BAD_REQUEST),
    ACCESS_TOKEN_NOT_EXIST("JWT0002", "액세스 토큰이 존재하지 않습니다.", HttpStatus.BAD_REQUEST),

    // conversation
    CONVERSATION_NOT_FOUND("CONVERSATION0001", "대화를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    CONVERSATION_ACCESS_DENIED("CONVERSATION0002", "해당 대화에 대한 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // rate limit
    RATE_LIMIT_EXCEEDED_MINUTE("RATE0001", "분당 요청 횟수 제한을 초과했습니다.", HttpStatus.TOO_MANY_REQUESTS),
    RATE_LIMIT_EXCEEDED_HOUR("RATE0002", "시간당 요청 횟수 제한을 초과했습니다.", HttpStatus.TOO_MANY_REQUESTS),
    RATE_LIMIT_EXCEEDED_DAILY("RATE0003", "일일 요청 횟수 제한을 초과했습니다.", HttpStatus.TOO_MANY_REQUESTS),
}
