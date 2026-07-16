# task_2 최종 보고: 공개 URL `/public` 경로 보정

## 배경

기존 파일 URL에 Drive 공개 서비스 경로인 `/public`이 누락되어 이미지가 표시되지 않았다.

## 원인 또는 설계 판단

`minio.public-url`을 사용하는 fallback이 객체 저장소 직접 경로 형식을 유지했다. 공개 URL은 저장소 endpoint가 아니라 Drive 공개 URL base에 `/public/{bucket}/{objectKey}`를 붙여야 한다.

## 변경 내용

- 공개 URL fallback도 `/public` 경로를 포함하도록 변경했다.
- S3 endpoint를 공개 URL fallback에서 제거했다.
- 분석 이미지 2건과 게시글 첨부 1건의 DB URL을 올바른 공개 경로로 복구했다.
- 재발 방지용 SQL 마이그레이션은 정규식 그룹 참조 대신 URL 구분자 분할로 origin을 보존한다.

## 검증 결과

- 분석 URL 2건과 게시글 첨부 URL 1건이 모두 `/public/tad/...` 형식임을 확인했다.
- 잘못된 `\\1` 경로가 남아 있지 않음을 확인했다.
- 전체 Gradle 테스트와 bootJar 빌드에 성공했다.

## 영향 범위

- 분석 이미지와 게시판 첨부의 기존·신규 공개 URL

## 남은 위험

- `minio.public-url` 또는 `minio.drive-public-url`은 Drive 공개 서비스 origin이어야 한다.

## 교훈

URL의 origin 보존이 필요한 데이터 마이그레이션에서는 정규식 replacement 이스케이프보다 버킷·객체 키와 URL 구성 요소를 명시적으로 조합하는 방식이 안전하다.

## 후속 작업

- 배포 후 브라우저에서 기존 이미지와 새 업로드 이미지를 확인한다.
