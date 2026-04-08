# Docker 환경 설정 가이드

## 준비 사항
- Docker Desktop
- Docker Compose

## 컨테이너 실행
```bash
docker-compose up -d
```

## 상태 확인
```bash
docker-compose ps
docker-compose logs -f
```

## PostgreSQL 접속
```bash
docker exec -it tad_postgres psql -U tad_app -d tad_db
```

## 서비스 정보
| 서비스 | 컨테이너명 | 포트 | 역할 |
| --- | --- | --- | --- |
| PostgreSQL | tad_postgres | 5001 | 메인 데이터베이스 |
| Redis | tad_redis | 6379 | 캐시 및 토큰 관리 |

## 접속 정보
**PostgreSQL**
- Host: `localhost:5001`
- User: `tad_app`
- Password: `tad_app_password`
- Database: `tad_db`

**Redis**
- Host: `localhost:6379`

## 애플리케이션 설정
[application.yml](/C:/develop/side-project/tad/TAD_web_back/src/main/resources/application.yml) 기준으로 PostgreSQL JDBC가 설정되어 있습니다.

## 종료
```bash
docker-compose down
docker-compose down -v
docker-compose stop
```

## 점검 명령
```bash
docker exec tad_postgres pg_isready -U tad_app -d tad_db
docker exec tad_redis redis-cli ping
```
