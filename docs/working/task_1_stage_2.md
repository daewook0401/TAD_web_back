# task_1 2단계 보고: 데이터베이스 마이그레이션 및 배포 검증

## 변경 파일

- `sql/board.sql`
- `sql/migrations/V20260716_01__add_attachment_object_locator.sql`
- `src/test/resources/application.properties`
- `docs/tech/tad-web-garage-configuration.md`

## 변경 내용

- 게시글·댓글 첨부 테이블에 `bucket`, `object_key`를 추가했다.
- 기존 첨부의 저장 파일명과 소유자 식별자로 객체 키를 복원했다.
- 복원 불가 행이 있으면 마이그레이션을 중단하도록 보호했다.
- 객체 위치 조회 인덱스를 추가했다.
- 배포 환경에 필요한 Garage·Drive 설정 키를 문서화했다.

## 실행한 검증

```text
docker exec -i postgres_server psql -v ON_ERROR_STOP=1 -U root -d tad_db < sql/migrations/V20260716_01__add_attachment_object_locator.sql
./gradlew clean test bootJar --no-daemon
```

## 검증 결과

- 운영 DB 마이그레이션 성공
- 기존 게시글 첨부 1건의 버킷과 객체 키 복원 성공
- 새 컬럼 NOT NULL 및 객체 위치 인덱스 확인
- 전체 Gradle 테스트 및 배포용 JAR 빌드 성공

## 특이사항

- 배포 서버에서 Garage endpoint를 확인할 수 없어 endpoint 값은 서버 설정에 반영하지 않았다.
- Garage 접근 키는 `tad` 버킷 RW 권한만 부여했고 GitHub Actions `main` 환경 비밀값에 저장했다.

## 다음 단계

- `main` 커밋·푸시 후 GitHub Actions 배포 상태 확인

## 승인

작업지시자의 `main` 푸시 승인에 따라 진행한다.
