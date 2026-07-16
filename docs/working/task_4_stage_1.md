# task_4 1단계 보고: 웹 백엔드 Garage S3 전환

## 변경 파일

- `build.gradle`
- `core/config/garage/*`
- 분석·게시판 저장소 참조 및 관련 테스트
- 테스트 설정과 GitHub Actions 배포 워크플로

## 변경 내용

- MinIO SDK를 AWS SDK S3 client로 교체했다.
- 저장소 설정 prefix와 클래스 명칭을 `garage`로 전환했다.
- Garage endpoint override, 정적 자격 증명, region, path-style 주소 지정을 적용했다.
- 배포 워크플로가 Garage 설정을 런타임 설정 파일로 제공하도록 변경했다.

## 실행한 검증

```text
./gradlew clean test bootJar --no-daemon
```

## 검증 결과

- 전체 Gradle 테스트 및 bootJar 빌드 성공
- 런타임 소스와 빌드 설정에서 MinIO 참조가 제거됐다.

## 특이사항

- 배포 키와 endpoint는 GitHub Actions 환경 비밀값으로만 제공한다.

## 배포 확인

- `main` 브랜치 배포 자동화가 성공했다.
- 배포 설정은 Garage S3 endpoint와 전용 자격 증명을 런타임 설정으로 제공한다.

## 승인

작업지시자의 Garage 연동 전환 지시에 따라 진행한다.
