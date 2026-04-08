# TAD Backend API Guideline

이 문서는 현재 백엔드에 구현된 API를 기준으로, 이후 엔드포인트를 추가하거나 다른 작업자에게 위임할 때 참고할 수 있는 가이드라인이다.

## Base Rules

- Base URL: `http://localhost:8080`
- 서버 `context-path`는 `/api` 이다.
- 외부 노출 URI는 모두 `/api/...` 형태다.
- 인증 API Prefix: `/api/auth/...`
- 게시판 API Prefix: `/api/board/...`
- 응답은 가능하면 JSON을 사용한다.
- 비즈니스 오류는 기본적으로 아래 형태를 따른다.

```json
{
  "success": false,
  "message": "오류 메시지"
}
```

## Auth APIs

### 1. 회원가입

- Method: `POST`
- URI: `/api/auth/signup`
- Request

```json
{
  "name": "tester",
  "email": "tester@example.com",
  "password": "password123"
}
```

- Response `201`

```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "user": {
    "id": 1,
    "name": "tester",
    "email": "tester@example.com",
    "memberRole": "ROLE_USER",
    "roles": ["ROLE_USER"]
  },
  "token": null,
  "refreshToken": null
}
```

- 규칙
  - 이메일 중복 불가
  - 이메일 인증 완료 상태여야 가입 가능
  - 가입 시 `auth.tb_user_role`에 `ROLE_USER` 자동 부여

### 2. 로그인

- Method: `POST`
- URI: `/api/auth/login`
- Request

```json
{
  "email": "tester@example.com",
  "password": "password123"
}
```

- Response `200`

```json
{
  "success": true,
  "message": "로그인에 성공했습니다.",
  "user": {
    "id": 1,
    "name": "tester",
    "email": "tester@example.com",
    "memberRole": "ROLE_USER",
    "roles": ["ROLE_USER"]
  },
  "token": "access-token",
  "refreshToken": "refresh-token"
}
```

- 규칙
  - JWT subject는 DB 컬럼이 아닌 Redis 세션용 `public_id(UUID)`를 사용
  - Redis에는 `public_id -> user_id`, `public_id -> refreshToken` 저장
  - JWT claim에 `roles` 포함

### 3. 토큰 재발급

- Method: `POST`
- URI: `/api/auth/refresh`
- Request

```json
{
  "refreshToken": "refresh-token"
}
```

- Response `200`

```json
{
  "accessToken": "new-access-token",
  "refreshToken": "new-refresh-token",
  "tokenType": "Bearer"
}
```

### 4. 이메일 인증 코드 발송

- Method: `POST`
- URI: `/api/auth/mail`
- Request

```json
{
  "email": "tester@example.com"
}
```

- Response `200`

```json
{
  "success": true,
  "message": "인증 코드가 발송되었습니다.",
  "email": "tester@example.com",
  "verified": false
}
```

### 5. 이메일 인증 코드 검증

- Method: `POST`
- URI: `/api/auth/mail/verify`
- Request

```json
{
  "email": "tester@example.com",
  "code": "123456"
}
```

- Response `200`

```json
{
  "success": true,
  "message": "이메일 인증이 완료되었습니다.",
  "email": "tester@example.com",
  "verified": true
}
```

- 규칙
  - 이메일 인증 상태는 Redis로 관리
  - `auth.tb_email_auth`는 현재 히스토리성 테이블 기준으로만 유지

## Board APIs

### 보드 종류

- `lol`
- `maple`
- `free`

카테고리 데이터 소스는 `board.tb_post_categories` 이다.

### 1. 카테고리 조회

- Method: `GET`
- URI: `/api/board/categories`
- 인증: 불필요

- Response `200`

```json
[
  {
    "id": 1,
    "categoryKey": "lol",
    "name": "롤",
    "iconUrl": "https://drive.towardadiamond.com/tad/category-icons/lol.webp",
    "summary": "롤 게시판",
    "displayOrder": 1
  },
  {
    "id": 2,
    "categoryKey": "maple",
    "name": "메이플랜드",
    "iconUrl": "https://drive.towardadiamond.com/tad/category-icons/maple.webp",
    "summary": "메이플랜드 게시판",
    "displayOrder": 2
  },
  {
    "id": 3,
    "categoryKey": "free",
    "name": "자유",
    "iconUrl": null,
    "summary": "자유 게시판",
    "displayOrder": 3
  }
]
```

### 2. 게시글 목록 조회

- Method: `GET`
- URI: `/api/board/posts`
- 인증: 불필요
- Query
  - `categoryKey`: 선택
  - `postType`: 선택, `all | free | info`
  - `page`: 선택, 기본 `0`
  - `size`: 선택, 기본 `20`

- Request Example

```http
GET /api/board/posts?categoryKey=lol&postType=free&page=0&size=20
```

- Response `200`

```json
{
  "items": [
    {
      "id": 101,
      "categoryKey": "lol",
      "categoryName": "롤",
      "title": "최신 메타 정리",
      "tag": "공략",
      "postType": "info",
      "viewCount": 320,
      "likeCount": 18,
      "replyCount": 7,
      "notice": true,
      "authorId": 3,
      "authorNickname": "관리자",
      "createdAt": "2026-04-08T13:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "hasNext": false
}
```

- 규칙
  - `postType` 데이터 소스는 `board.tb_post.post_type`
  - 현재 값은 소문자 `free`, `info`
  - `notice`는 별도 컬럼 `is_notice`
  - 정렬 기준
    - `is_notice DESC`
    - `post_type ASC`
    - `created_at DESC`

### 3. 게시글 상세 조회

- Method: `GET`
- URI: `/api/board/posts/{postId}`
- 인증: 불필요

- Request Example

```http
GET /api/board/posts/101
```

- Response `200`

```json
{
  "id": 101,
  "categoryId": 1,
  "categoryKey": "lol",
  "categoryName": "롤",
  "title": "최신 메타 정리",
  "content": "게시글 본문 내용",
  "tag": "공략",
  "postType": "info",
  "viewCount": 321,
  "likeCount": 18,
  "replyCount": 7,
  "notice": true,
  "authorId": 3,
  "authorNickname": "관리자",
  "createdAt": "2026-04-08T13:00:00",
  "updatedAt": "2026-04-08T13:10:00"
}
```

- 규칙
  - 상세 조회 시 `view_count` 1 증가

## Security Rules

- 내부 시큐리티 매처 기준 `/auth/**`: 공개
- 내부 시큐리티 매처 기준 `/board/**`: 공개
- 그 외: 인증 필요

JWT 인증 시 `roles` claim을 `GrantedAuthority`로 변환하므로, 이후 서버 권한 체크는 아래처럼 추가 가능하다.

```java
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAuthority('ROLE_USER')")
```

## DB Mapping Rules

- 인증 스키마: `auth`
- 게시판 스키마: `board`
- 엔티티에는 스키마명을 반드시 명시한다.

예시:

```java
@Table(name = "tb_user", schema = "auth")
@Table(name = "tb_post", schema = "board")
```

## Follow-up Recommendation

다음 구현 우선순위:

1. 게시글 작성 API
2. 댓글 목록/작성 API
3. 게시글 좋아요 API
4. 게시판 프론트 연동
