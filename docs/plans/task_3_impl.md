# task_3 구현계획서: 게시판 카테고리 아이콘 공개 URL 보정

## 분석 결과

- 프론트 화면은 `boardAPI.getCategories()` 응답의 `iconUrl`을 직접 사용한다.
- 운영 DB의 아이콘 URL 2건은 `/tad/category-icons/...` 형식이다.

## 확정 설계

- 기존 URL origin은 유지하고, 버킷 경로 앞에 `/public`을 넣는다.
- `tad` 버킷 카테고리 아이콘만 대상으로 제한한다.

## 수정 예정 파일

- `sql/board.sql`
- `sql/migrations/V20260716_03__repair_category_icon_urls.sql`

## 단계 분할

1. 시드 및 마이그레이션 변경
2. 운영 DB 적용
3. 테스트·`main` 푸시

## 테스트 계획

- 변경된 DB URL 경로 검증
- 전체 Gradle 테스트 및 bootJar 빌드

## 위험 요소

- 카테고리 아이콘 URL이 Drive 공개 URL base가 아닌 경우 표시가 실패할 수 있다.

## 승인

작업지시자의 추가 작업 지시로 승인되었다.
