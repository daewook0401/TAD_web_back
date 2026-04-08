# TAD Web Backend

Spring Boot 3 기반의 TAD 백엔드 API 서버입니다.

## 핵심 스택
- Java 21
- Spring Boot 3.5
- Spring Security
- Spring Data JPA
- Spring Data Redis
- PostgreSQL
- JWT

## 실행 환경
- API 기본 포트: `8080`
- 서버 context-path: `/api`
- 기본 API 주소: `http://localhost:8080/api`

## 데이터 저장소
- PostgreSQL: 메인 애플리케이션 데이터 저장
- Redis: refresh token, 이메일 인증 코드, 캐시 저장

현재 런타임 설정은 [application.yml](/C:/develop/side-project/tad/TAD_web_back/src/main/resources/application.yml) 기준으로 PostgreSQL JDBC를 사용합니다.

## 로컬 실행
```bash
./gradlew bootRun
```

## 주요 API Prefix
- 인증: `/api/auth/**`
- 게시판: `/api/board/**`

## 보안 규칙
- `/auth/**` 공개
- `/board/**` 공개
- 그 외 경로는 인증 필요

## 문서
- Docker 실행 가이드: [DOCKER_SETUP.md](/C:/develop/side-project/tad/TAD_web_back/DOCKER_SETUP.md)
- API 가이드: [API_GUIDELINE.md](/C:/develop/side-project/tad/TAD_web_back/API_GUIDELINE.md)
