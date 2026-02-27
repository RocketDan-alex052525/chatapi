# ChatAPI

OpenAI API Key를 등록하고 로그인하여 AI와 대화를 나눌 수 있는 챗봇 서버입니다.

## 기술 스택

- **Language**: Kotlin 1.9.25
- **Framework**: Spring Boot 3.5.10
- **Database**: PostgreSQL 17
- **In-Memory Store**: Redis
- **Auth**: JWT

## 요구사항

- Java 17
- Docker & Docker Compose

## 로컬 실행

### 1. 저장소 클론

```bash
git clone https://github.com/alex052525/chatapi.git
cd chatapi
```

### 2. 환경변수 설정

`.env`

```env
# Database
POSTGRES_DB=chatapi
POSTGRES_USER=db_user
POSTGRES_PASSWORD=db_password

# Security
## API_KEY_SECRET: OpenAI API 키 조회용 HmacSHA256 서명 키 (임의의 문자열)
API_KEY_SECRET=hmac_secret
## API_KEY_ENC_SECRET: OpenAI API 키 저장용 AES-GCM 암호화 키 (임의의 문자열)
API_KEY_ENC_SECRET=aes_enc_secret
## JWT_SECRET: JWT 서명 키 (최소 32자 이상의 임의의 문자열)
JWT_SECRET=your_jwt_secret

# OpenAI
OPENAI_API_KEY=openai_api_key
```

### 3. 인프라 실행 (PostgreSQL, Redis)

```bash
docker-compose up -d
```

### 4. 서버 실행

```bash
./gradlew bootRun
```


## API 사용법

- **로컬**: http://localhost:8080/swagger-ui.html
- **운영**: https://chatapi-ktn8.onrender.com/swagger-ui.html

### 사용자 흐름

```
1. POST /api/auth/apiKey   - OpenAI API Key 등록
2. GET  /api/auth/login    - 로그인 (JWT 쿠키 발급)
3. POST /api/conversations - 대화 생성
4. POST /api/chat/completions - 메시지 전송 및 AI 응답 수신
5. POST /api/chat/completions/stream - 메시지 전송 및 AI 응답 스트리밍 수신
```

### 인증

`/api/auth/**`를 제외한 모든 API는 로그인 후 발급된 JWT 쿠키(`ACCESS_TOKEN_COOKIE`)가 필요합니다.

### Rate Limit

| 단위 | 제한 |
|------|------|
| 분   | 5회  |
| 시간 | 30회 |
| 일   | 100회 |

## 헬스체크

```
로컬: http://localhost:8080/management/health
운영: https://chatapi-ktn8.onrender.com/management/health
```

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 클래스
./gradlew test --tests "com.rkd.chatapi.chat.service.ChatCompletionServiceTest"

# 특정 메서드
./gradlew test --tests "com.rkd.chatapi.chat.service.ChatCompletionServiceTest.methodName"
```

### 커버리지

```bash
# 테스트 실행 + 커버리지 리포트 생성
./gradlew test jacocoTestReport
```

리포트 위치: `build/reports/jacoco/test/html/index.html`
