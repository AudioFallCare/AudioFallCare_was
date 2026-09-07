# AudioFallCare — Backend (WAS)

소리 기반 AI 낙상 감지 시스템 AudioFallCare의 백엔드 서버입니다.
IoT 리코더가 보낸 오디오를 AI 서버가 분석해 낙상을 감지하면, 이 서버가 기록을 저장하고 보호자에게 푸시 알림을 보냅니다.

## 개요

혼자 사는 노약자·환자 공간에서 낙상 사고가 발생했을 때, 사람이 상시 지켜보지 않아도 자동으로 감지해 보호자에게 즉시 알리는 것이 이 서비스의 목적입니다.

```
IoT 리코더 → (WebSocket, 3초 단위 오디오) → AI 서버(FastAPI)
                                                 │ 낙상 판정 시
                                                 ▼
                                     Spring 백엔드 (POST /api/internal/fall)
                                                 │
                                        DB 저장 + FCM 푸시 발송
                                                 ▼
                                          보호자 모바일 앱
```

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language / Framework | Java 17, Spring Boot 3.5.10 |
| 인증 | Spring Security + JWT (jjwt) |
| DB | PostgreSQL, Spring Data JPA |
| 캐시 | Redis |
| 알림 | Firebase Admin SDK (FCM) |
| API 문서 | springdoc-openapi (Swagger UI) |
| 기타 | Lombok, MapStruct |
| 인프라 | Docker, GitHub Actions CI, Render |

## 도메인 구조

| 도메인 | 설명 |
|---|---|
| `auth` | 회원가입/로그인/로그아웃, JWT 발급 및 갱신 |
| `code` | IoT 리코더 ↔ 계정 페어링용 6자리 연결 코드 발급·검증 |
| `recorder` | 리코더 등록/조회/상태 관리 |
| `internal` | AI 서버 전용 내부 API (API Key 인증, 낙상 이벤트 수신) |
| `history` | 낙상 이력 조회, 월간 통계 |
| `alert` | 알림 목록 조회, 읽음 처리, 지난달 대비 낙상 증감 비교 |
| `fcm` | FCM 디바이스 토큰 등록 |
| `global` | 헬스체크 등 공통 기능 |

## 주요 API

```
# 인증
POST   /api/auth/signup
POST   /api/auth/login
POST   /api/auth/logout
POST   /api/auth/refresh

# 연결 코드
GET    /api/code
POST   /api/code/generate
POST   /api/code/regenerate
POST   /api/code/verify

# 리코더
POST   /api/recorders
GET    /api/recorders
GET    /api/recorders/{id}/status
GET    /api/recorders/{recorderId}/user

# 낙상 이력 / 알림
GET    /api/histories
GET    /api/histories/stats
GET    /api/alerts
GET    /api/alerts/fall-diff

# AI 서버 → 백엔드 내부 통신 (API Key 인증)
POST   /api/internal/fall

# FCM
POST   /api/fcm/token
```

## 인증 구조

- **사용자**: JWT Access Token + HttpOnly 쿠키 기반 Refresh Token
- **AI 서버 → 백엔드**: 별도 API Key 인증 (`/api/internal/**`) — 사용자 인증과 통신 경로 자체를 분리해 두 영역의 보안 요구사항을 명확히 나눴습니다.

## 로컬 실행

```bash
# 1. DB/Redis만 띄우기
docker compose up postgres redis

# 2. 환경변수 설정 (.env.example 참고 후 .env 작성)

# 3. 서버 실행
./gradlew bootRun
# → http://localhost:8080
# → API 문서: http://localhost:8080/swagger-ui.html
```

전체 스택(AI 서버 포함)을 한 번에 띄우려면:

```bash
docker compose --profile full up
```

## CI/CD

- `.github/workflows/ci.yml` — PR/푸시 시 자동 빌드
- `.github/workflows/code-review.yml`, `pr-labeler.yml` — PR 자동 라벨링 및 리뷰 워크플로우
- GitHub Secrets 기반 환경변수 주입, fork PR 대응 처리
- Render를 통한 배포 자동화

## 관련 저장소

- [AudioFallCare_ai](https://github.com/AudioFallCare/AudioFallCare_ai) — AI 추론 서버
- [AudioFallCare_web](https://github.com/AudioFallCare/AudioFallCare_web) — 프론트엔드
- [AudioFallcare_docs](https://github.com/AudioFallCare/AudioFallcare_docs) — 문서
