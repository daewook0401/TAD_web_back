# task_1 최종 보고: Garage S3 및 Drive 공개 URL 연동

## 배경

기존 파일 업로드는 객체 저장소 주소를 직접 DB에 저장했다. Drive 서비스가 공개 버킷 정책을 관리하도록 전환하면서 저장 위치와 공개 URL의 책임을 분리했다.

## 설계 판단

- 객체의 원본 식별자는 버킷과 객체 키다.
- 웹 공개 URL은 Drive 서비스의 공개 경로로 동적으로 생성한다.
- 웹 백엔드는 `tad` 버킷에 한정된 S3 권한만 가진다.
- 공개 URL 설정 전 배포에서도 기존 첨부가 깨지지 않도록 기존 URL 생성 방식을 한시적으로 유지한다.

## 변경 내용

- S3 호환 저장 서비스에서 자동 버킷 생성을 제거했다.
- Drive 공개 URL base 설정을 추가했다.
- 분석 이미지 응답을 버킷·객체 키 기반의 동적 공개 URL로 전환했다.
- 게시글·댓글 첨부 엔티티에 `bucket`, `object_key`를 추가했다.
- 기존 첨부 객체 위치를 복원하는 운영 DB 마이그레이션을 적용했다.
- Garage `tad` 버킷 전용 읽기·쓰기 키를 발급하고 배포 환경 비밀값에 보관했다.

## 검증 결과

```text
./gradlew clean test bootJar --no-daemon
```

- 전체 Gradle 테스트 성공
- 배포용 JAR 빌드 성공
- 운영 DB 마이그레이션 성공
- 기존 게시글 첨부 1건의 객체 위치 복원 및 인덱스 확인 성공
- GitHub Actions 자동 배포 성공

## 영향 범위

- 분석 이미지와 게시판 첨부 API 응답에 `bucket`, `objectKey`가 추가된다.
- 새로운 공개 URL은 Drive 공개 경로 형식을 사용한다.

## 남은 위험

- 배포 컨테이너에 Garage endpoint 및 Drive 공개 URL base를 제공하지 않으면 기존 객체 URL 호환 모드가 유지된다.
- Garage endpoint가 배포 서버에서 네트워크로 접근 가능한지는 서버 설정에서 확인해야 한다.

## 교훈

객체 저장소 endpoint는 서버 간 저장 API 용도이고, 사용자에게 노출하는 URL은 별도 공개 서비스 경로로 관리해야 변경에 안전하다.

## 후속 작업

- 배포 컨테이너에 `MINIO_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_BUCKET`, `MINIO_DRIVE_PUBLIC_URL`을 제공한다.
- 전환 후 새 업로드와 기존 첨부의 공개 URL을 브라우저에서 확인한다.
