# Screenmate Backend Server

데스크탑 펫 앱 **Screenmate**의 백엔드 서버입니다.

## 기술 스택

- **Framework**: Spring Boot 4.0.2
- **Language**: Java 21
- **Database**: PostgreSQL (AWS RDS)
- **Authentication**: Google OAuth 2.0 + JWT
- **Documentation**: Swagger/OpenAPI 3.0
- **Migration**: Flyway

---

## 빠른 시작

### 1. 환경 변수 설정

프로젝트 루트에 `.env` 파일 생성:

```bash
# Database (PostgreSQL RDS)
RDS_HOST=your-rds-endpoint.amazonaws.com
RDS_USERNAME=your_username
RDS_PASSWORD=your_password

# JWT
JWT_SECRET=your-256-bit-secret-key-here-minimum-32-characters

# Google OAuth
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com

# OpenAI
OPENAI_API_KEY=sk-your-openai-api-key
```

### 2. 실행

```bash
# 개발 환경
./gradlew bootRun

# 프로덕션 빌드
./gradlew build
java -jar build/libs/screenmate-0.0.1-SNAPSHOT.jar
```

### 3. API 문서 확인

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

---

## 아키텍처

### 패키지 구조

```
moleep.screenmate/
├── config/              # 설정 클래스
│   ├── SecurityConfig   # Spring Security + JWT 필터
│   ├── JwtProperties    # JWT 설정값
│   ├── OpenAiProperties # OpenAI 설정값
│   ├── WebClientConfig  # OpenAI WebClient
│   ├── RateLimitConfig  # 분당 60회 제한
│   └── JacksonConfig    # JSON 직렬화 설정
│
├── security/
│   ├── jwt/             # JWT 토큰 처리
│   │   ├── JwtTokenProvider      # 토큰 생성/검증
│   │   └── JwtAuthenticationFilter
│   └── oauth/
│       └── GoogleIdTokenVerifier # Google ID Token 검증
│
├── domain/              # JPA 엔티티 + Repository
│   ├── user/            # 사용자
│   ├── token/           # Refresh Token
│   ├── character/       # 캐릭터
│   ├── memory/          # QA 메모리
│   └── event/           # 이벤트 로그
│
├── dto/                 # Request/Response DTO
│   ├── auth/
│   ├── sync/
│   └── llm/
│
├── service/             # 비즈니스 로직
│   ├── auth/            # 인증 서비스
│   ├── sync/            # 동기화 서비스
│   └── llm/             # LLM 프록시 서비스
│
├── controller/          # REST API 엔드포인트
├── validation/          # 검증 로직
└── exception/           # 예외 처리
```

---

## 핵심 설계

### 1. 단일 기기 로그인 (Single Device Login)

한 계정은 **하나의 기기에서만** 로그인할 수 있습니다.

```
[기기 A 로그인] → refresh_token 발급
[기기 B 로그인] → 기기 A의 refresh_token 무효화 → 기기 B에 새 토큰 발급
[기기 A 토큰 갱신 시도] → 401 Unauthorized (device_id 불일치)
```

**구현 방식:**
- `refresh_tokens.user_id`에 UNIQUE 제약
- 새 로그인 시 기존 토큰 UPSERT (덮어쓰기)
- 토큰 갱신 시 `device_id` 일치 여부 검증

### 2. 토큰 관리

| 토큰 | 만료 시간 | 용도 |
|------|----------|------|
| Access Token | 15분 | API 인증 (Authorization 헤더) |
| Refresh Token | 30일 | Access Token 재발급 |

**토큰 갱신 플로우:**
```
POST /auth/refresh
{
  "refreshToken": "...",
  "deviceId": "device-uuid-12345"  // 로그인 시 사용한 것과 동일해야 함
}
```

### 3. QA 메모리 낙관적 락 (Optimistic Locking)

캐릭터가 학습한 정보(QA 메모리)의 동시 수정 충돌을 방지합니다.

```
[클라이언트 A] GET /sync/bootstrap → qaMemory.version = 3
[클라이언트 B] GET /sync/bootstrap → qaMemory.version = 3
[클라이언트 A] PATCH /characters/{id}/qa (expectedVersion=3) → 성공, version=4
[클라이언트 B] PATCH /characters/{id}/qa (expectedVersion=3) → 409 Conflict
```

**충돌 시 클라이언트 대응:**
1. 최신 데이터 다시 조회
2. 변경사항 머지
3. 새 버전으로 재시도

### 4. LLM 프록시 보안

| 정책 | 설명 |
|------|------|
| 스크린샷 처리 | 메모리에서만 처리, 디스크/DB 저장 금지 |
| 이미지 제한 | 최대 5MB, PNG/JPEG/GIF/WebP만 허용 |
| Rate Limit | 사용자당 분당 60회 |
| 액션 화이트리스트 | `APPEAR_EDGE`, `PLAY_ANIM`, `SPEAK`, `MOVE`, `EMOTE`, `SLEEP` |
| QA 키 제한 | `user_`, `pref_`, `fact_`, `memory_`, `context_` prefix만 허용 |
| QA 값 제한 | 최대 500자 |

LLM 응답의 `qaPatch`는 서버가 자동으로 저장합니다. 클라이언트가 사전 준비한 질문/지식만 `/characters/{id}/qa`로 패치하면 됩니다.

---

## 데이터베이스 스키마

### ERD 개요

```
users (1) ──────── (1) refresh_tokens
  │
  └── (1) ──────── (N) characters
                        │
                        ├── (1) ── (1) character_qa_memories
                        │
                        └── (1) ── (N) character_events
```

### 테이블 상세

#### users
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | UUID | PK |
| email | VARCHAR(255) | 구글 이메일 (UNIQUE) |
| name | VARCHAR(255) | 사용자 이름 |
| profile_image_url | TEXT | 프로필 이미지 |
| created_at | TIMESTAMP | 가입일 |
| updated_at | TIMESTAMP | 수정일 |

#### refresh_tokens
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | UUID | PK |
| user_id | UUID | FK → users (UNIQUE) |
| token | VARCHAR(512) | Refresh Token 값 |
| device_id | VARCHAR(255) | 기기 고유 ID |
| device_name | VARCHAR(255) | 기기 이름 |
| expires_at | TIMESTAMP | 만료 시간 |
| created_at | TIMESTAMP | 생성일 |

#### characters
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | UUID | PK |
| user_id | UUID | FK → users |
| name | VARCHAR(100) | 캐릭터 이름 |
| species | VARCHAR(50) | 종류 |
| personality | TEXT | 성격 |
| happiness | INTEGER | 행복도 (0-100) |
| hunger | INTEGER | 배고픔 (0-100) |
| health | INTEGER | 건강 (0-100) |
| stage_index | INTEGER | 성장 단계 (0-3, 감소 불가) |
| is_alive | BOOLEAN | 생존 여부 |
| died_at | TIMESTAMP | 사망 시각 |
| created_at | TIMESTAMP | 생성일 |
| updated_at | TIMESTAMP | 수정일 |

#### character_qa_memories
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | UUID | PK |
| character_id | UUID | FK → characters (UNIQUE) |
| qa_data | JSONB | 학습된 Q&A 데이터 |
| version | BIGINT | 낙관적 락 버전 |
| updated_at | TIMESTAMP | 수정일 |

#### character_events
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | UUID | PK |
| character_id | UUID | FK → characters |
| event_type | VARCHAR(50) | 이벤트 타입 |
| event_text | VARCHAR(1000) | 이벤트 내용 |
| metadata | TEXT | 추가 메타데이터 (JSON) |
| occurred_at | TIMESTAMP | 발생 시각 |

**이벤트 타입:**
- `EVOLUTION` - 진화
- `DEATH` - 사망
- `FEEDING` - 먹이주기
- `PLAYING` - 놀아주기
- `SPEAKING` - 대화
- `EMOTION` - 감정 변화
- `MILESTONE` - 마일스톤 달성
- `CUSTOM` - 커스텀 이벤트

---

## 상태 검증 규칙

### 캐릭터 상태

| 필드 | 범위 | 특수 규칙 |
|------|------|----------|
| happiness | 0-100 | - |
| hunger | 0-100 | - |
| health | 0-100 | - |
| stage_index | 0-3 | **감소 불가** (진화는 되돌릴 수 없음) |
| is_alive | boolean | `false`로 변경 시 `died_at` 필수 |

### QA 메모리

| 규칙 | 설명 |
|------|------|
| 키 prefix | `user_`, `pref_`, `fact_`, `memory_`, `context_` 중 하나로 시작 |
| 값 길이 | 최대 500자 |
| 버전 검증 | `expectedVersion`이 현재 버전과 일치해야 함 |

---

## API 엔드포인트 요약

### 인증 (Auth)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/auth/google` | Google 로그인 |
| POST | `/auth/refresh` | 토큰 갱신 |
| POST | `/auth/logout` | 로그아웃 |

### 동기화 (Sync)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/sync/bootstrap` | 전체 데이터 조회 |
| POST | `/characters` | 캐릭터 생성 |
| PATCH | `/characters/{id}` | 캐릭터 상태 업데이트 |
| PATCH | `/characters/{id}/qa` | QA 메모리 패치 |
| POST | `/characters/{id}/events` | 이벤트 기록 |

### 친구 (Friend)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/characters/search` | 캐릭터 검색 (이름/종족/유저명/초대코드) |
| POST | `/characters/{id}/friend-requests` | 친구 요청 보내기 |
| GET | `/characters/{id}/friend-requests` | 친구 요청 목록 |
| POST | `/characters/{id}/friend-requests/{requestId}/accept` | 친구 요청 수락 |
| POST | `/characters/{id}/friend-requests/{requestId}/reject` | 친구 요청 거절 |
| GET | `/characters/{id}/friends` | 친구 목록 조회 |
| POST | `/characters/{id}/friends/{friendId}/messages` | 메시지/이모티콘 보내기 |
| GET | `/characters/{id}/friends/{friendId}/messages` | 메시지 목록 조회 |

### LLM 프록시

| Method | Endpoint | Content-Type | 설명 |
|--------|----------|--------------|------|
| POST | `/llm/generate` | application/json | 텍스트만 |
| POST | `/llm/generate` | multipart/form-data | 스크린샷 포함 |

---

## 개발 환경 전용 API

프로덕션이 아닌 환경에서만 사용 가능한 테스트용 API입니다.

### 테스트 로그인

Google OAuth 없이 테스트용 토큰을 발급받습니다.

```bash
POST /dev/auth/test-login?email=test@example.com&deviceId=test-device
```

**응답:**
```json
{
  "accessToken": "eyJhbG...",
  "refreshToken": "eyJhbG...",
  "expiresIn": 900,
  "user": {
    "id": "uuid",
    "email": "test@example.com",
    "name": "Test User"
  }
}
```

> **주의:** `@Profile("!prod")`로 설정되어 프로덕션 환경에서는 자동으로 비활성화됩니다.

---

## 에러 응답 형식

모든 에러는 일관된 형식으로 반환됩니다.

```json
{
  "code": "ERROR_CODE",
  "message": "사람이 읽을 수 있는 에러 메시지"
}
```

### 주요 에러 코드

| HTTP | Code | 설명 |
|------|------|------|
| 400 | `INVALID_GOOGLE_TOKEN` | 유효하지 않은 Google ID Token |
| 400 | `VALIDATION_FAILED` | 요청 데이터 검증 실패 |
| 400 | `INVALID_STAGE_TRANSITION` | 잘못된 성장 단계 변경 (감소 시도) |
| 400 | `DEATH_REQUIRES_DIED_AT` | 사망 처리 시 died_at 누락 |
| 400 | `INVALID_QA_KEY_PREFIX` | 허용되지 않은 QA 키 prefix |
| 401 | `INVALID_TOKEN` | 유효하지 않은 토큰 |
| 401 | `TOKEN_EXPIRED` | 만료된 토큰 |
| 401 | `DEVICE_MISMATCH` | 기기 ID 불일치 (다른 기기에서 로그인됨) |
| 403 | `ACCESS_DENIED` | 접근 권한 없음 |
| 404 | `USER_NOT_FOUND` | 사용자 없음 |
| 404 | `CHARACTER_NOT_FOUND` | 캐릭터 없음 |
| 409 | `VERSION_CONFLICT` | QA 메모리 버전 충돌 |
| 429 | `RATE_LIMIT_EXCEEDED` | API 호출 한도 초과 |

---

## 배포

### EC2 배포 예시

```bash
# 1. JAR 빌드
./gradlew build

# 2. EC2로 전송
scp build/libs/screenmate-0.0.1-SNAPSHOT.jar ec2-user@your-ec2:/home/ec2-user/

# 3. EC2에서 실행
ssh ec2-user@your-ec2
export RDS_HOST=your-rds-endpoint
export RDS_USERNAME=your_username
export RDS_PASSWORD=your_password
export JWT_SECRET=your-secret
export GOOGLE_CLIENT_ID=your-client-id
export OPENAI_API_KEY=your-api-key

java -jar screenmate-0.0.1-SNAPSHOT.jar
```

### systemd 서비스 등록 (선택)

`/etc/systemd/system/screenmate.service`:

```ini
[Unit]
Description=Screenmate Backend
After=network.target

[Service]
User=ec2-user
WorkingDirectory=/home/ec2-user
ExecStart=/usr/bin/java -jar screenmate-0.0.1-SNAPSHOT.jar
EnvironmentFile=/home/ec2-user/.env
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl enable screenmate
sudo systemctl start screenmate
sudo systemctl status screenmate
```

---

## 보안 체크리스트

- [ ] `JWT_SECRET`은 최소 256비트 (32자 이상)
- [ ] 환경 변수는 `.env` 파일로 관리, **절대 커밋하지 않음**
- [ ] RDS 보안 그룹에서 EC2 IP만 허용
- [ ] 프로덕션에서 `/dev/**` 엔드포인트 비활성화 확인
- [ ] HTTPS 적용 (프로덕션)
- [ ] CORS 설정 확인

---

## 라이선스

Private - All rights reserved
