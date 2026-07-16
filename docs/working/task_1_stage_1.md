# task_1 1단계 보고: 저장소와 공개 URL 책임 분리

## 변경 파일

- `core/config/minio/MinioProperties.java`
- `core/config/minio/MinioStorageService.java`
- 분석 응답 DTO 및 서비스
- 게시판 첨부 DTO·엔티티·서비스
- 관련 단위 테스트

## 변경 내용

- S3 endpoint와 Drive 공개 URL base 설정을 분리했다.
- 공개 URL은 `{drivePublicUrl}/public/{bucket}/{encodedObjectKey}`로 조합한다.
- 웹 서비스의 자동 버킷 생성 동작을 제거했다.
- 분석 및 게시판 응답은 DB에 저장된 버킷·객체 키로 공개 URL을 동적으로 만든다.
- 게시판 첨부에 버킷과 객체 키를 저장하도록 변경했다.

## 실행한 검증

```text
./gradlew test --tests com.tad.www.core.config.minio.MinioStorageServiceTest --tests com.tad.www.api.board.service.BoardAttachmentServiceTest --no-daemon
```

## 검증 결과

- 성공
- 한글·공백 객체 키의 URL 인코딩을 확인했다.
- 게시판 첨부의 버킷·객체 키 저장과 공개 URL 반환을 확인했다.

## 특이사항

- `minio.drive-public-url` 미설정 환경은 기존 객체 URL 방식을 유지한다.

## 다음 단계

- 운영 DB 마이그레이션 적용 및 전체 빌드 검증

## 승인

작업지시자의 구현 승인에 따라 다음 단계를 진행했다.
