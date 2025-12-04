# TAD Web Backend

TAD(Technology Advancement Development) 프로젝트의 백엔드 서비스입니다.

## 📋 프로젝트 개요

Spring Boot 3.5.7 기반의 RESTful API 서버로, JWT 인증, WebSocket 지원, Redis 캐싱 등의 기능을 제공합니다.

## 🛠️ 기술 스택

### Core Framework
- **Spring Boot**: 3.5.7
- **Java**: 21
- **Build Tool**: Gradle

### Core Dependencies
- **Spring Security**: 보안 및 인증
- **JWT (jjwt)**: 토큰 기반 인증 (0.12.3)
- **Spring Data JPA**: ORM 및 데이터 접근 계층
- **Spring Data Redis**: 토큰 캐싱 및 관리
- **Jedis**: Redis 클라이언트
- **WebSocket**: 실시간 통신
- **Lombok**: 보일러플레이트 코드 감소

### Database
- **MySQL**: 주 데이터베이스 (JPA)
- **Redis**: JWT 토큰 캐싱 및 토큰 블랙리스트 관리

### Testing
- **JUnit 5**: 단위 테스트
- **Spring Boot Test**: 통합 테스트

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/com/tad/www/
│   │   ├── WwwApplication.java
│   │   ├── api/
│   │   │   ├── advice/              # Global Exception Handler
│   │   │   └── v1/
│   │   │       └── auth/
│   │   │           └── controller/   # 인증 관련 컨트롤러
│   │   ├── common/                  # 공통 유틸리티
│   │   ├── configuration/           # 설정 클래스
│   │   ├── core/
│   │   │   └── config/
│   │   │       └── security/        # 보안 설정
│   │   │           ├── SecurityConfigure.java
│   │   │           └── jwt/
│   │   │               ├── JwtFilter.java
│   │   │               └── JwtUtil.java
│   │   └── infra/                   # 인프라 계층
│   └── resources/
│       ├── application.yml
│       └── application-private.yml
└── test/
    └── java/com/tad/www/
        └── WwwApplicationTests.java
```

## 🚀 시작하기

### 사전 요구사항
- Java 21 이상
- Gradle 7.x 이상
- MySQL 8.0 이상
- Redis 6.0 이상 (선택적)

### 설치 및 실행

1. **저장소 클론**
```bash
git clone <repository-url>
cd TAD_web_back
```

2. **의존성 설치**
```bash
./gradlew build
```

3. **환경 설정**
`application-private.yml` 파일에서 데이터베이스, Redis 등의 연결 정보를 설정합니다.

4. **애플리케이션 실행**
```bash
./gradlew bootRun
```

서버는 기본적으로 `http://localhost:8080`에서 실행됩니다.

## 🔐 인증 시스템

### JWT 기반 인증
- **JwtUtil**: JWT 토큰 생성, 검증, 파싱
- **JwtFilter**: 요청 필터링 및 토큰 검증
- **SecurityConfigure**: Spring Security 설정
- **Redis**: 토큰 저장소 및 블랙리스트 관리

### 인증 플로우
1. 사용자 로그인 → JWT 토큰 발급 및 Redis에 저장
2. 요청 시 Authorization 헤더에 토큰 포함
3. JwtFilter에서 토큰 검증 (Redis 확인)
4. 유효한 토큰일 경우 요청 처리
5. 로그아웃 시 Redis에서 토큰 블랙리스트 추가

## 🔄 WebSocket

실시간 양방향 통신을 지원합니다.
- 채팅, 알림, 실시간 데이터 동기화 등에 활용

## 💾 데이터베이스

### JPA (Java Persistence API)
- 객체 관계 매핑 (ORM)
- 선언적 쿼리 지원
- 자동 테이블 생성 및 관리

### Redis 토큰 관리
- JWT 토큰 캐싱
- 토큰 블랙리스트 관리 (로그아웃)
- 빠른 토큰 검증

## 📝 API 문서

### Auth API (`/api/v1/auth`)
- `POST /login` - 사용자 로그인
- `POST /register` - 사용자 회원가입
- `POST /refresh` - 토큰 갱신
- `POST /logout` - 로그아웃

> 📌 **추가 예정인 엔드포인트**
> - 비밀번호 재설정
> - 이메일 인증
> - OAuth 연동

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests WwwApplicationTests
```

## 📦 배포

### 실행 가능한 JAR 생성
```bash
./gradlew bootJar
```

생성된 JAR 파일은 `build/libs/` 디렉토리에 위치합니다.

## 🔄 CI/CD

> 📌 **구성 예정**
> - GitHub Actions 워크플로우
> - 자동 테스트 및 빌드
> - 배포 자동화

## 📚 추가 기능 (개발 예정)

- [ ] 사용자 프로필 관리
- [ ] 역할 기반 접근 제어 (RBAC)
- [ ] 토큰 갱신 (Refresh Token)
- [ ] API 속도 제한 (Rate Limiting)
- [ ] 로깅 및 모니터링
- [ ] 문서 생성 자동화 (Swagger/OpenAPI)
- [ ] 이벤트 기반 아키텍처
- [ ] 마이크로서비스 통신

## 🤝 기여

이 프로젝트는 개인 프로젝트입니다.

## 📧 연락처

문의 사항이 있으시면 연락주세요.

## 📄 라이센스

이 프로젝트는 개인 사용을 위한 비공개 프로젝트입니다.

---

**마지막 업데이트**: 2025년 12월 4일
