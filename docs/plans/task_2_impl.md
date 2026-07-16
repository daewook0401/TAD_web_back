# task_2 구현계획서: 기존 공개 URL의 `/public` 경로 보정

## 분석 결과

- 모든 대상 행은 버킷 `tad`와 객체 키를 보유한다.
- 기존 URL은 동일 origin의 `/tad/{objectKey}` 형식이다.
- 원본 URL을 문자열 치환하는 대신 버킷·객체 키로 경로를 재구성하면 중복 삽입을 막을 수 있다.

## 확정 설계

- `minio.drive-public-url`이 없으면 `minio.public-url`을 Drive 공개 URL base로 사용한다.
- S3 endpoint는 공개 URL 생성 fallback으로 사용하지 않는다.
- 기존 DB URL은 origin을 유지하고 `/public/{bucket}/{objectKey}`로 재구성한다.

## 수정 예정 파일

- `core/config/minio/MinioStorageService.java`
- 저장 서비스 단위 테스트
- SQL 마이그레이션 파일
- 관련 기술 문서와 최종 보고서

## 단계 분할

1. URL 생성 코드·테스트 수정
2. 운영 DB URL 마이그레이션 적용
3. 전체 검증·`main` 푸시

## 테스트 계획

- `minio.public-url`만 설정됐을 때 `/public`이 포함되는지 검증
- 운영 DB에서 모든 대상 URL의 경로 검증
- 전체 테스트와 bootJar 빌드

## 위험 요소

- 공개 URL base가 Drive 서비스 origin이 아닌 값이면 파일 제공이 실패한다.

## 승인

작업지시자의 추가 작업 지시로 승인되었다.
