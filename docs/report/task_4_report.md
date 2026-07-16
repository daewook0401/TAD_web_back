# task_4 완료보고: 웹 백엔드 Garage S3 전환

## 결과

- 객체 저장소 SDK를 MinIO 전용 구현에서 AWS SDK S3 client로 교체했다.
- 설정 키와 클래스 명칭을 `garage`로 통일하고 path-style Garage S3 연결을 적용했다.
- 분석 이미지와 게시판 첨부파일 저장소 접근을 Garage 서비스로 전환했다.
- 배포 자동화가 Garage 설정을 런타임 설정 파일에 제공하도록 변경했다.

## 검증

- 전체 Gradle 테스트 및 bootJar 빌드에 성공했다.
- 런타임 소스와 빌드 설정에서 MinIO 참조가 남아 있지 않음을 확인했다.
- `main` 브랜치 배포 자동화가 성공했다.

## 보안

- Garage endpoint와 접근 자격 증명은 배포 비밀값으로만 관리한다.

## 승인

작업지시자의 Garage 전환 및 MinIO 삭제 승인에 따라 완료한다.
