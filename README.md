# Rocketdan Frontend

OpenAI API 키를 이용한 개인화 AI 채팅 웹 애플리케이션입니다.
사용자가 자신의 OpenAI API 키를 등록하고, 여러 대화를 생성, 관리하며 AI와 대화할 수 있습니다.

---

## 주요 기능

### 인증 (API 키 기반)

- OpenAI API 키를 서버에 등록하거나, 등록된 키로 로그인
- 로그인 상태는 만료 시간과 함께 로컬 스토리지에 저장되어 자동 유지

<img width="1356" height="1034" alt="image" src="https://github.com/user-attachments/assets/e3ccff20-1647-44f0-b148-55a61094ed3f" />

---

### 대화 목록 관리

- 새로운 대화 생성 (제목 지정)
- 기존 대화 목록 조회 (cursor 기반 페이징)
- 대화 삭제

<img width="1353" height="1032" alt="스크린샷 2026-02-27 오후 12 34 38" src="https://github.com/user-attachments/assets/ae322fc4-2323-41cc-864d-c9cfdd6b7f19" />

---

### 메시지 채팅

- 선택한 대화의 이전 메시지 히스토리 불러오기
- 메시지 입력 및 전송
- **일반 모드**: AI 응답을 완성 후 한 번에 수신
- **스트리밍 모드**: AI 응답을 실시간으로 글자 단위로 수신

<img width="1392" height="1031" alt="스크린샷 2026-02-27 오후 12 35 45" src="https://github.com/user-attachments/assets/86f3548b-924d-4954-8ca2-b46bcf5406f7" />

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| UI 프레임워크 | React 18 |
| 언어 | TypeScript 5.7 |
| 빌드 도구 | Vite 6 |
---

## 시작하기

### 사전 요구사항

- Node.js 18 이상
- npm
- 백엔드 서버 (`http://localhost:8080`)

### 설치 및 실행

```bash
# 의존성 설치
npm install

# 개발 서버 실행 (http://localhost:5173)
npm run dev
```

### 빌드

```bash
npm run build
```

---

## 사용 흐름

1. **인증 페이지** (`/auth`)에서 OpenAI API 키를 등록하거나 로그인
2. **채팅 페이지** (`/chat`)로 이동
3. 왼쪽 사이드바에서 **새 대화** 생성
4. 메시지 입력 후 **전송** (스트리밍 모드 선택 가능)
5. AI 응답 확인 — 스트리밍 모드에서는 실시간으로 출력됨
