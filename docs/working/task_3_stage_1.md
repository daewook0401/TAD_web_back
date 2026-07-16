# task_3 1단계 보고: 카테고리 아이콘 공개 URL 보정

## 변경 파일

- `sql/board.sql`
- `sql/migrations/V20260716_03__repair_category_icon_urls.sql`

## 변경 내용

- 신규 카테고리 시드의 아이콘 URL을 Drive 공개 경로로 변경했다.
- 기존 카테고리 아이콘 URL을 origin·객체 경로 기준으로 재구성하는 마이그레이션을 추가했다.

## 실행한 검증

```text
docker exec -i postgres_server psql -v ON_ERROR_STOP=1 -U root -d tad_db < sql/migrations/V20260716_03__repair_category_icon_urls.sql
```

## 검증 결과

- 운영 DB 카테고리 아이콘 2건 보정 성공
- 이전 `/tad/...` 형식 0건 확인
- `/public/tad/category-icons/...` 형식 2건 확인

## 다음 단계

- 백엔드 빌드·`main` 푸시 및 자동 배포 확인

## 승인

작업지시자의 추가 작업 지시로 진행한다.
