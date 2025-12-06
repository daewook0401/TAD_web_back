# Docker 환경 설정 가이드

## 📋 요구사항
- Docker Desktop 설치
- Docker Compose 설치

## 🚀 실행 방법

### 1. Docker 컨테이너 실행
```bash
# 현재 디렉토리: back/www
docker-compose up -d
```

### 2. 실행 상태 확인
```bash
# 실행 중인 컨테이너 확인
docker-compose ps

# 로그 확인
docker-compose logs -f
```

### 3. 데이터베이스 초기화 (선택사항)
```bash
# MySQL 접속
docker exec -it tad_mysql mysql -u root -p

# 패스워드: root
# 그 후 SQL 쿼리 실행
```

## 🔧 주요 설정

### 서비스 상세 정보
| 서비스 | 컨테이너명 | 포트 | 역할 |
|--------|----------|------|------|
| Redis | tad_redis | 6379 | 캐싱 및 토큰 관리 |
| MySQL | tad_mysql | 3306 | 메인 데이터베이스 |

### 접속 정보
**MySQL:**
- Host: localhost:3306
- User: tad_user
- Password: tad_password
- Database: tad_db

**Redis:**
- Host: localhost:6379
- Password: (없음)

## 📝 Spring Boot 설정

`application-local.properties`에 Docker 환경 설정이 포함되어 있습니다.

Spring Boot 실행 시 다음 옵션 사용:
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

또는 IDE에서:
```
VM options: -Dspring.profiles.active=local
```

## 🛑 중지 및 정리

```bash
# 컨테이너 중지
docker-compose down

# 컨테이너 및 볼륨 제거 (데이터 삭제)
docker-compose down -v

# 컨테이너만 중지 (데이터 유지)
docker-compose stop
```

## 🔍 문제 해결

### Redis 연결 실패
```bash
# Redis 상태 확인
docker exec tad_redis redis-cli ping
```

### MySQL 연결 실패
```bash
# MySQL 상태 확인
docker exec tad_mysql mysqladmin ping -u root -proot
```

### 포트 충돌
`docker-compose.yml`에서 포트 번호를 변경:
```yaml
ports:
  - "6380:6379"  # 외부 포트:내부 포트
```

## 📦 데이터 영속성

- `redis_data`: Redis 데이터 디렉토리
- `mysql_data`: MySQL 데이터 디렉토리

Docker 볼륨으로 관리되므로 컨테이너 삭제 후 재시작해도 데이터가 유지됩니다.
