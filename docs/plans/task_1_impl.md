# task_1 구현계획서: Garage S3 및 Drive 공개 URL 연동

## 분석 결과

- 현재 S3 호환 클라이언트는 Garage에서도 그대로 사용할 수 있다.
- 분석 게임은 `bucket`, `object_key`를 이미 저장한다.
- 게시글과 댓글 첨부는 `file_url`만 저장하므로 객체를 안정적으로 식별할 수 없다.
- 공개 URL은 저장소 endpoint가 아닌 Drive 공개 서비스 base URL을 기준으로 생성해야 한다.

## 확정 설계

- 환경 설정에는 S3 endpoint와 Drive 공개 URL base를 분리한다.
- 새로운 파일은 항상 설정된 버킷과 객체 키를 원본 식별자로 저장한다.
- URL은 공개 버킷인 경우에만 Drive 공개 URL base로 조합한다.
- 게시판 첨부에는 `bucket`, `object_key`를 추가하고, `file_url`은 기존 클라이언트 호환용으로 유지한다.
- 버킷 생성은 별도 Drive 관리 영역의 책임이므로 웹 서비스의 자동 생성 동작을 제거한다.

## 수정 예정 파일

- `core/config/minio/MinioProperties.java`
- `core/config/minio/MinioStorageService.java`
- 게시판 첨부 엔티티·서비스·응답 DTO
- 관련 단위 테스트
- `sql/board.sql` 및 별도 SQL 마이그레이션 파일
- 운영 DB

## 단계 분할

1. 설정 및 저장 서비스 변경
2. 게시판 데이터 모델·SQL 마이그레이션 변경
3. 운영 DB 반영 및 테스트·빌드
4. 커밋·`main` 푸시

## 테스트 계획

- 저장 서비스의 공개 URL 인코딩과 버킷 자동 생성 비활성화 테스트
- 게시판 첨부의 버킷·객체 키 저장 테스트
- 전체 테스트와 bootJar 빌드

## 위험 요소

- 운영 DB의 기존 URL 형식이 하나로 고정되어 있지 않으면 객체 키 자동 추출이 불완전할 수 있다.
- Garage 권한 키에는 대상 버킷에 대한 최소 권한만 부여해야 한다.

## 승인

작업지시자의 코드 수정·DB 반영·`main` 푸시 지시로 승인되었다.
