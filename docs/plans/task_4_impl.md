# task_4 구현계획서: Garage S3 저장소 전환 및 MinIO 제거 준비

## 분석 결과

- 기존 Java 클라이언트는 MinIO SDK에 직접 결합돼 있다.
- Garage는 S3 호환 API를 제공하므로 AWS SDK S3 클라이언트와 path-style 주소 지정을 사용한다.
- 웹 백엔드는 `tad` 버킷에 대한 읽기·쓰기만 필요하다.

## 확정 설계

- `GarageProperties`, `GarageConfig`, `GarageStorageService`로 명칭과 설정 prefix를 전환한다.
- AWS SDK S3 client에 endpoint override, 정적 자격 증명, path-style 주소 지정을 적용한다.
- MinIO 의존성을 제거한다.
- GitHub Actions 환경 비밀값을 통해 Garage 런타임 설정을 생성한다.

## 수정 예정 파일

- `build.gradle`
- 기존 `core/config/minio/*` 및 참조 서비스·테스트
- `.github/workflows/deploy.yml`
- 테스트 환경 설정

## 테스트 계획

- 공개 URL 생성 단위 테스트
- 전체 Gradle 테스트와 bootJar 빌드
- Garage 객체 업로드·읽기 확인

## 위험 요소

- 배포 서버에서 Garage endpoint에 접근하지 못하면 신규 업로드가 실패한다.
- 런타임 설정 파일의 기존 애플리케이션 설정을 보존해야 한다.

## 승인

작업지시자의 Garage 연동 전환 지시로 승인되었다.
