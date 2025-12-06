# 1. 📘 시스템 개요

TAD Web Backend는 **Spring Boot 3.5.7** 기반의 RESTful API 서버로, 다음을 목표로 설계되었습니다.

- 사용자 인증 및 권한 관리 (JWT 기반)
- MySQL + JPA 기반의 안정적인 데이터 관리
- Redis 기반의 캐싱 및 토큰/세션 관리
- WebSocket을 통한 실시간 데이터 통신
- 높은 유지보수성, 확장성을 가진 모듈 구조

본 문서는 **설계 단계(TAD Architecture)** 기준으로 작성되었습니다.

# 2. 🧱 전체 아키텍처 구조

## ✔ High-Level Architecture

```markdown
[Client (Web/Native)] 
        │
        ▼
[API Gateway / Nginx]
        │
        ▼
[Spring Boot Backend]
  ├── API Layer (v1)
  ├── Application/Domain Layer
  ├── Security Layer (JWT)
  ├── Persistence Layer (JPA)
  ├── Mapper Layer (MyBatis)
  └── WebSocket Layer
        │
        ├── MySQL (Master DB)
        ├── Redis (Token/Cache)
        └── MongoDB (선택적 세션 저장)
```

## ✔ 주요 컴포넌트 역할

| 컴포넌트 | 역할 |
| --- | --- |
| Spring Boot API | REST API / 인증 / 비즈니스 로직 |
| Security Layer | JWT 인증/인가 처리 |
| MySQL | 주요 데이터 저장 |
| JPA | 엔티티 기반 ORM 처리 |
| MyBatis | 복잡한 SQL, 조회 성능 최적화 |
| Redis | Refresh Token, 인증 코드, 캐싱 |
| WebSocket | 실시간 알림·동기화 |

# 3. 🛠 기술 스택 상세

## 3.1 Backend Core

- Spring Boot 3.5.7
- Java 21
- Gradle
- Spring Security
- JJWT 0.12.3
- Spring Data JPA
- MyBatis
- Spring Data Redis
- WebSocket (Stomp)

---

# 4. 📁 패키지 구조 (아키텍처 Layering)

```
com.tad.www
├── WwwApplication.java
├── api/
│   ├── advice/             # GlobalExceptionHandler
│   └── v1/
│       └── auth/
│           └── controller/  # 인증 도메인 API
├── common/                 # 공통 util, response, enums
├── configuration/          # Spring, Redis, Origin 설정
├── core/
│   └── config/security/    # Security + JWT 설정
│       ├── SecurityConfigure.java
│       ├── JwtFilter.java
│       └── JwtUtil.java
└── infra/                  # DB, Redis, Mapper

```

---

# 5. 🔐 인증 아키텍처 (JWT + Redis)

## 5.1 Access / Refresh Token 전략

| 항목 | 저장 위치 | TTL | 설명 |
| --- | --- | --- | --- |
| Access Token | 클라이언트 | 15~30분 | API 요청 시 인증 |
| Refresh Token | Redis | 7~14일 | 토큰 재발급용 |
| Blacklist Token | Redis | Access Token 잔여 기간 | 로그아웃, 강제 만료 |

### Redis Key 구조 예시

```
auth:refresh:{memberId}
auth:blacklist:{accessToken}
auth:code:{email}

```

---

## 5.2 인증 플로우

```
[Login Request]
        │
        ▼
User 인증 → JWT 발급
        │
        ├── Access Token → Client
        └── Refresh Token → Redis 저장

```

**재발급**

1. 클라이언트 → `/refresh`
2. Redis에서 Refresh Token 조회 및 검증
3. Access Token 재발급

**로그아웃**

1. Access Token → Redis 블랙리스트 저장
2. Refresh Token 삭제

---

# 6. 💾 데이터베이스 설계 (MySQL + JPA)

## 6.1 설계 원칙

- 모든 테이블 UTF8MB4
- `id(PK)`, `created_at`, `updated_at`, `status` 공통 적용
- 연관관계는 **단방향 지향**
- 비밀번호는 BCrypt로 암호화
- JPA는 트랜잭션 기반의 CRUD 중심
- 복잡한 조회 = MyBatis로 분리

---

## 6.2 주요 엔티티(예시)

### Member

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | BIGINT | PK |
| username | VARCHAR(50) | UNIQUE |
| password | VARCHAR(255) | BCrypt |
| email | VARCHAR(255) |  |
| role | ENUM | USER/ADMIN |
| status | ENUM | ACTIVE/DELETED |
| created_at | DATETIME |  |
| updated_at | DATETIME |  |

### AuthLog (선택)

로그/감사 기록 확장 가능

---

# 7. 💨 Redis & 캐싱 설계

## Redis 사용 목적

| 종류 | 용도 |
| --- | --- |
| Token Store | Refresh Token / Blacklist |
| 인증 번호 | 이메일·휴대폰 인증 |
| 세션 | Spring Session 사용 시 |
| 캐시 | 인증 사용자 정보, 빈번 조회 데이터 |

---

# 8. 🔊 WebSocket 설계

## WebSocket 사용 목적

- 실시간 알림
- 채팅
- 실시간 상태 업데이트

## 연결 구조

```
Client
  ↕ STOMP
WebSocket Controller
  ↕
Message Broker

```

---

# 9. 🧪 테스트 전략

### 단위 테스트

- JUnit 5
- Mockito
- Security 테스트용 MockUser

### 통합 테스트

- SpringBootTest
- WebMvcTest
- Embedded Redis (선택)

### 추가 예정

- 부하 테스트 (k6, JMeter)

---

# 10. 📦 배포 & 운영

## 빌드

```bash
./gradlew bootJar

```

## 설정 파일 구조

```
application.yml
application-private.yml
└── DB, Redis, JWT secrets 관리

```

## 향후 CI/CD 구성

- GitHub Actions → Build & Test
- Docker Image Build
- Server Deploy 자동화

---

# 11. 📘 API 설계 (요약)

### Auth API (`/api/v1/auth`)

| Method | URI | 설명 |
| --- | --- | --- |
| POST | /login | 로그인 |
| POST | /register | 회원가입 |
| POST | /refresh | 토큰 재발급 |
| POST | /logout | 로그아웃 |

---

# 12. 📡 장애 대응 전략

| 장애 | 대응 방식 |
| --- | --- |
| Redis 다운 | Refresh Token 만료 → 재로그인 요구 |
| DB 슬로우쿼리 | 인덱스 튜닝, Query 분석 |
| 과도한 요청 | Rate Limit / WebSecurity 설정 |
| JWT 탈취 | 블랙리스트 + 강제 로그아웃 |

---

# 13. 🛠 향후 확장 계획

- RBAC(Role-Based Access Control)
- Swagger/OpenAPI 문서 자동화
- API Rate Limiting
- 이벤트 기반 아키텍처 도입
- MSA 전환 가능 구조 탐색

---

# 14. 📄 부록

- 마지막 업데이트: **2025-12-04**
- 작성: 김대욱