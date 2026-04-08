# TAD Backend API Roadmap

현재 기준에서 아직 남아 있는 작업을 정리한 체크리스트입니다.

## 1. 인증 API 정리

- [ ] 인증 응답 형식 전체 점검
- [ ] 인증 API URI 규칙 최종 정리
- [ ] 인증 관련 예외 메시지와 상태 코드 일관화

## 2. 메일 발송 고도화

- [ ] 운영 SMTP 설정 정리
- [ ] 발송 실패 처리 개선
- [ ] 재발송 제한 정책 추가

## 3. 로그아웃 API

- [ ] `/api/auth/logout` 추가
- [ ] Redis refresh token 정리 처리

## 4. 이메일 중복 확인 API

- [ ] `/api/auth/email/check` 추가
- [ ] 프론트 회원가입 화면과 연동

## 5. Google 로그인 검증 구현

- [ ] Google credential 검증
- [ ] `auth.tb_oauth_account` 연동
- [ ] 기존 계정과 소셜 계정 연결 정책 정리

## 6. JWT/Refresh 보안 강화

- [ ] refresh token rotation 정책 재점검
- [ ] 만료/재사용 예외 처리 보강
- [ ] 로그아웃 이후 토큰 무효화 검증

## 7. 권한 기반 접근 제어

- [ ] 관리자 전용 API 권한 설정
- [ ] `@PreAuthorize` 적용 확대
- [ ] 권한 테스트 추가

## 8. 게시글 작성 API

- [ ] `/api/board/posts` `POST`
- [ ] category, title, content, tag, postType 입력 처리
- [ ] 작성자 인증 정보 매핑

## 9. 게시글 수정 API

- [ ] `/api/board/posts/{postId}` `PUT` 또는 `PATCH`
- [ ] 작성자 본인 수정 권한 확인
- [ ] 수정 시 `updated_at` 반영 확인

## 10. 게시글 삭제 API

- [ ] `/api/board/posts/{postId}` `DELETE`
- [ ] soft delete 처리
- [ ] 작성자와 관리자 삭제 권한 정책 정리

## 11. 댓글 목록 조회 API

- [ ] `/api/board/posts/{postId}/comments` `GET`
- [ ] 부모 댓글/대댓글 구조 설계
- [ ] 삭제 댓글 표시 정책 정리

## 12. 댓글 작성 API

- [ ] `/api/board/posts/{postId}/comments` `POST`
- [ ] 일반 댓글/대댓글 처리
- [ ] `reply_count` 반영

## 13. 댓글 수정 및 삭제 API

- [ ] 댓글 수정 API
- [ ] 댓글 삭제 API
- [ ] 삭제 시 `reply_count` 처리 정책 정리

## 14. 게시글 좋아요 API

- [ ] `/api/board/posts/{postId}/like`
- [ ] `board.tb_post_like` 연동
- [ ] 중복 좋아요 방지
- [ ] `like_count` 반영

## 15. 게시글 검색 및 정렬 고도화

- [ ] 키워드 검색
- [ ] 작성자 검색
- [ ] 정렬 조건 확장
- [ ] 페이지 응답 확장

## 16. 공지 처리 정책 정리

- [ ] 공지 작성 권한 정의
- [ ] 카테고리별 공지 노출 규칙 정리
- [ ] `is_notice` 와 `post_type` 역할 구분 확정

## 17. 프론트 연동

- [ ] `BoardPage.jsx` 샘플 데이터 제거
- [ ] 카테고리 API 연결
- [ ] 게시글 목록 API 연결
- [ ] 게시글 상세 페이지 연결

## 18. DTO/응답 형식 정리

- [ ] 성공 응답 형식 통일
- [ ] 목록 응답 구조 정리
- [ ] 상세 응답 구조 정리

## 19. 예외 처리 정리

- [ ] `GlobalExceptionHandler` 확장
- [ ] 인증/게시판 예외 구분
- [ ] 400/401/403/404/500 응답 정책 확정

## 20. 테스트 코드 추가

- [ ] 인증 API 테스트
- [ ] 게시판 조회 API 테스트
- [ ] 게시글/댓글/좋아요 API 테스트
- [ ] 권한 테스트

## 21. 이력성 테이블 검토

- [ ] `auth.tb_login_history` 도입 검토
- [ ] `auth.tb_token_history` 도입 검토
- [ ] 필요 시 `auth.tb_email_auth` 이력 저장 정리

## 22. 운영 설정 분리

- [ ] local/dev/prod 설정 분리
- [ ] 메일/Redis/JWT 환경변수 정리
- [ ] JPA `ddl-auto` 운영 정책 분리

## 23. API 문서 보강

- [ ] `API_GUIDELINE.md` 최신화 유지
- [ ] 게시글 작성/댓글/좋아요 API 문서 추가
- [ ] 필요 시 Swagger/OpenAPI 도입
