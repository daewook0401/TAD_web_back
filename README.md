# TAD Web Backend

Spring Boot 3 기반의 TAD 백엔드 API 서버입니다.

## 기술 스택

- Java 21
- Spring Boot 3.5
- Spring Security
- Spring Data JPA
- Spring Data Redis
- PostgreSQL
- JWT

## 저장소 역할

- PostgreSQL: 사용자, 게시글, 댓글 등 서비스 데이터 저장
- Redis: refresh token, 이메일 인증 코드, 인증 관련 캐시 저장

## 실행 방법

```bash
./gradlew bootRun
```

Windows 환경에서는 아래 명령을 사용합니다.

```bash
.\gradlew.bat bootRun
```

로컬 DB/Redis와 로컬 yml의 MinIO endpoint에 붙여 실행할 때는 `local` 프로필을 사용합니다.
분석 API가 떠 있지 않은 환경에서는 업로드/분석 호출을 제외한 기능부터 확인합니다.

```powershell
$env:SPRING_PROFILES_ACTIVE='local'
$env:SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/tad'
$env:SPRING_DATASOURCE_USERNAME='tad'
$env:SPRING_DATASOURCE_PASSWORD='tad'
$env:SPRING_DATA_REDIS_HOST='localhost'
$env:SPRING_DATA_REDIS_PORT='6379'
$env:MINIO_ENDPOINT='https://drive.towardadiamond.com'
$env:MINIO_PUBLIC_URL='https://drive.towardadiamond.com'
$env:MINIO_ACCESS_KEY='minioadmin'
$env:MINIO_SECRET_KEY='minioadmin'
$env:MINIO_BUCKET='tad'
# 분석 API가 있을 때만 지정합니다.
# $env:ANALYSIS_SERVICE_URL='http://localhost:8000'
.\gradlew.bat bootRun
```

로컬 프로필 설정 파일은 `src/main/resources/application-local.yml`입니다.
프론트 기본 API 주소가 `http://localhost:8080/api`이므로 로컬 프로필은 context-path를 `/api`로 맞춥니다.

## 테스트

```bash
.\gradlew.bat test
```

## 주요 API Prefix

- 인증: `/api/auth/**`
- 게시판: `/api/board/**`

## 보안 규칙

공개 경로

- `/auth/login`
- `/auth/signup`
- `/auth/refresh`
- `/auth/mail`
- `/auth/mail/verify`
- `/auth/google-login`
- `GET /board/**`
- `/health`

인증 필요 경로

- `/auth/me`
- `/auth/me/password`
- 그 외 공개로 허용되지 않은 모든 경로

## JWT 인증 구조

로그인 성공 시 백엔드는 아래 토큰을 발급합니다.

- `accessToken`
- `refreshToken`

토큰 생성은 [src/main/java/com/tad/www/core/config/security/jwt/JwtUtil.java](/C:/develop/side-project/tad/TAD_web_back/src/main/java/com/tad/www/core/config/security/jwt/JwtUtil.java)에서 처리합니다.

JWT의 `subject`에는 사용자 PK를 직접 넣지 않고, 로그인 세션 식별용 `publicId(UUID)`를 사용합니다.

Redis에는 아래 정보가 저장됩니다.

- `publicId -> userId`
- `publicId -> refreshToken`

이 구조 덕분에 토큰 자체만 믿지 않고 Redis 저장 상태를 함께 확인할 수 있습니다.

## Access Token 만료 시 Refresh 흐름

refresh 처리 엔드포인트는 `POST /api/auth/refresh`입니다.

요청 예시는 아래와 같습니다.

```json
{
  "refreshToken": "refresh-token"
}
```

성공 시 응답은 아래와 같습니다.

```json
{
  "accessToken": "new-access-token",
  "refreshToken": "new-refresh-token",
  "tokenType": "Bearer"
}
```

동작 순서는 아래와 같습니다.

1. 프론트가 만료된 `accessToken`으로 보호 API를 호출합니다.
2. `JwtFilter`가 access token을 검사하다가 만료되면 `401`과 `ACCESS_TOKEN_EXPIRED`를 반환합니다.
3. 프론트는 저장된 `refreshToken`으로 `/auth/refresh`를 호출합니다.
4. `JwtRefreshService`는 refresh token의 서명, 타입, Redis 저장값 일치 여부를 검증합니다.
5. 검증에 성공하면 기존 Redis 세션을 지우고 새 `accessToken`과 새 `refreshToken`을 발급합니다.
6. 새 refresh token을 Redis에 다시 저장합니다.
7. 프론트는 새 토큰을 저장한 뒤 원래 요청을 다시 호출합니다.

즉, refresh token도 재발급되는 rotation 방식입니다.

## 관련 구현 위치

- 보안 필터: [src/main/java/com/tad/www/core/config/security/jwt/JwtFilter.java](/C:/develop/side-project/tad/TAD_web_back/src/main/java/com/tad/www/core/config/security/jwt/JwtFilter.java)
- refresh API: [src/main/java/com/tad/www/api/auth/controller/AuthController.java](/C:/develop/side-project/tad/TAD_web_back/src/main/java/com/tad/www/api/auth/controller/AuthController.java)
- refresh 서비스: [src/main/java/com/tad/www/api/auth/service/JwtRefreshService.java](/C:/develop/side-project/tad/TAD_web_back/src/main/java/com/tad/www/api/auth/service/JwtRefreshService.java)
- Redis 저장 서비스: [src/main/java/com/tad/www/api/auth/service/RefreshTokenRedisService.java](/C:/develop/side-project/tad/TAD_web_back/src/main/java/com/tad/www/api/auth/service/RefreshTokenRedisService.java)

## 문서

- API 가이드: [API_GUIDELINE.md](/C:/develop/side-project/tad/TAD_web_back/API_GUIDELINE.md)
