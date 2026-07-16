# task_2 1단계 보고: 공개 URL 생성 및 DB URL 보정

## 변경 파일

- `core/config/minio/MinioStorageService.java`
- `MinioStorageServiceTest.java`
- `sql/migrations/V20260716_02__repair_public_file_urls.sql`

## 변경 내용

- `minio.drive-public-url`이 없을 때 `minio.public-url`을 Drive 공개 URL base로 사용한다.
- 공개 URL은 항상 `/public/{bucket}/{objectKey}` 형식으로 조합한다.
- 기존 분석·게시판 첨부 URL을 버킷·객체 키 기준으로 재구성한다.
- SQL의 origin 추출은 정규식 치환 참조 대신 URL 구분자 분할을 사용한다.

## 실행한 검증

```text
./gradlew clean test bootJar --no-daemon
```

## 검증 결과

- 전체 테스트 및 bootJar 빌드 성공
- 공개 URL fallback 단위 테스트가 `/public` 경로를 검증한다.

## 특이사항

- 최초 SQL 적용 직후 검증에서 잘못된 정규식 참조 문자열이 발견됐다.
- 즉시 버킷·객체 키 기준으로 운영 DB 3건을 복구했고, 잘못된 SQL은 커밋 전에 안전한 방식으로 교체했다.

## 다음 단계

- `main` 푸시 및 자동 배포 확인

## 승인

작업지시자의 추가 작업 지시로 진행한다.
