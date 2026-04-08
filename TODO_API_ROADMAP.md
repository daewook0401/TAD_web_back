# TAD Backend API Roadmap

현재 기준에서 아직 남아 있는 작업을 정리한 문서다.
우선순위와 구현 범위를 빠르게 확인할 수 있도록 목차형 체크리스트로 관리한다.

## 1. 인증 API 정리

- [ ] 인증 응답 포맷 전체 재점검
- [ ] 인증 API URI 규칙 일관성 재검토
- [ ] 인증 관련 예외 메시지/상태코드 표준화

## 2. 이메일 발송 실제 구현

- [ ] `MailService.send()` 실제 메일 발송 연결
- [ ] 발송 실패 처리 로직 추가
- [ ] 재발송 제한 정책 추가

## 3. 로그아웃 API 추가

- [ ] `/api/auth/logout` 추가
- [ ] Redis refresh/session 키 삭제 처리

## 4. 이메일 중복 확인 API 추가

- [ ] `/api/auth/email/check` 추가
- [ ] 프론트 회원가입 화면과 연결

## 5. Google 로그인 실제 검증 구현

- [ ] Google credential 검증
- [ ] `auth.tb_oauth_account` 연동
- [ ] 기존 계정과 소셜 계정 연결 정책 정리

## 6. JWT/Refresh 보안 강화

- [ ] refresh token rotation 정책 재점검
- [ ] 만료/재사용 탐지 정책 추가
- [ ] 로그아웃 이후 토큰 무효화 검토

## 7. 권한 기반 접근 제어 적용

- [ ] 관리자 전용 API 권한 설정
- [ ] `@PreAuthorize` 기반 보호 적용
- [ ] 권한별 테스트 추가

## 8. 게시글 작성 API

- [ ] `/api/board/posts` `POST`
- [ ] category, title, content, tag, postType 입력 처리
- [ ] 작성자 인증 사용자 매핑

## 9. 게시글 수정 API

- [ ] `/api/board/posts/{postId}` `PUT` 또는 `PATCH`
- [ ] 작성자 본인 수정 권한 체크
- [ ] 수정 시 `updated_at` 반영 확인

## 10. 게시글 삭제 API

- [ ] `/api/board/posts/{postId}` `DELETE`
- [ ] soft delete 처리
- [ ] 작성자/관리자 삭제 권한 정책 적용

## 11. 댓글 목록 조회 API

- [ ] `/api/board/posts/{postId}/comments` `GET`
- [ ] 부모 댓글/대댓글 구조 응답 설계
- [ ] 삭제 댓글 표시 정책 정리

## 12. 댓글 작성 API

- [ ] `/api/board/posts/{postId}/comments` `POST`
- [ ] 일반 댓글/대댓글 작성 처리
- [ ] `reply_count` 반영

## 13. 댓글 수정/삭제 API

- [ ] 댓글 수정 API
- [ ] 댓글 삭제 API
- [ ] 삭제 시 `reply_count` 처리 정책 정리

## 14. 게시글 좋아요 API

- [ ] `/api/board/posts/{postId}/like` 추가
- [ ] `board.tb_post_like` 연동
- [ ] 중복 좋아요 방지
- [ ] `like_count` 반영

## 15. 게시글 검색/정렬 고도화

- [ ] 키워드 검색
- [ ] 작성자 검색
- [ ] 정렬 조건 확장
- [ ] 페이징 응답 확장

## 16. 카테고리별 공지 처리 정책 정리

- [ ] 공지 작성 권한 정의
- [ ] 카테고리별 공지 노출 규칙 정리
- [ ] `is_notice`와 `post_type` 역할 구분 확정

## 17. 게시판 프론트 연동

- [ ] `BoardPage.jsx` 샘플 데이터 제거
- [ ] 카테고리 API 연결
- [ ] 게시글 목록 API 연결
- [ ] 게시글 상세 페이지 연결

## 18. DTO/응답 포맷 일관화

- [ ] 성공 응답 래핑 여부 통일
- [ ] 목록 응답 표준화
- [ ] 상세 응답 표준화

## 19. 예외 처리 표준화

- [ ] `GlobalExceptionHandler` 확장
- [ ] 인증/게시판 예외 구분
- [ ] 400/401/403/404/500 응답 정책 확정

## 20. 테스트 코드 추가

- [ ] 인증 API 테스트
- [ ] 게시판 조회 API 테스트
- [ ] 게시글/댓글/좋아요 API 테스트
- [ ] 권한 테스트

## 21. DB 히스토리 테이블 적재 로직 추가

- [ ] `auth.tb_login_history` 저장
- [ ] `auth.tb_token_history` 저장
- [ ] 필요 시 `auth.tb_email_auth` 히스토리 적재

## 22. OAuth 계정 연동 테이블 사용 시작

- [ ] `auth.tb_oauth_account` 엔티티 추가
- [ ] Google 로그인 시 계정 연결 저장

## 23. 운영용 설정 분리

- [ ] local/dev/prod 설정 분리
- [ ] 메일/Redis/JWT 보안값 환경변수화
- [ ] JPA `ddl-auto` 운영 정책 분리

## 24. API 문서화 보강

- [ ] `API_GUIDELINE.md` 최신화 유지
- [ ] 게시판 작성/댓글/좋아요 API 문서 추가
- [ ] 필요 시 Swagger/OpenAPI 도입

## 권장 우선순위

1. 게시글 작성 API
2. 댓글 목록/작성 API
3. 게시글 좋아요 API
4. 게시판 프론트 연동
5. 테스트 코드 추가

## 메모

- 현재 인증 기본 흐름은 동작 가능 상태다.
- 현재 게시판은 조회 API까지만 구현된 상태다.
- 다음 구현은 보통 게시글 작성부터 시작하는 것이 자연스럽다.
