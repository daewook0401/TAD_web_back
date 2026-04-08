# TAD Backend API Guideline

현재 백엔드에 구현된 API를 기준으로 정리한 문서입니다.

## Base Rules

- Base URL: `http://localhost:8080`
- 서버 `context-path`는 `/api` 입니다.
- 실제 호출 URI는 모두 `/api/...` 형태입니다.
- 인증 API Prefix: `/api/auth/...`
- 게시판 API Prefix: `/api/board/...`
- 응답은 가능하면 JSON을 사용합니다.
- 비즈니스 오류는 기본적으로 아래 형식을 따릅니다.

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

Request

```json
{
  "name": "tester",
  "email": "tester@example.com",
  "password": "password123"
}
```

Response `201`

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

규칙

- 이미 가입된 이메일은 사용할 수 없습니다.
- 이메일 인증이 완료된 상태여야 가입 가능합니다.
- 가입 시 `ROLE_USER` 권한이 기본 부여됩니다.

### 2. 로그인

- Method: `POST`
- URI: `/api/auth/login`

Request

```json
{
  "email": "tester@example.com",
  "password": "password123"
}
```

Response `200`

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

규칙

- JWT subject는 user id가 아니라 Redis 세션의 `public_id(UUID)`를 사용합니다.
- Redis에는 `public_id -> user_id`, `public_id -> refreshToken` 이 저장됩니다.
- JWT claim에는 `roles`가 포함됩니다.

### 3. 토큰 재발급

- Method: `POST`
- URI: `/api/auth/refresh`

Request

```json
{
  "refreshToken": "refresh-token"
}
```

Response `200`

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

Request

```json
{
  "email": "tester@example.com"
}
```

Response `200`

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

Request

```json
{
  "email": "tester@example.com",
  "code": "123456"
}
```

Response `200`

```json
{
  "success": true,
  "message": "이메일 인증이 완료되었습니다.",
  "email": "tester@example.com",
  "verified": true
}
```

규칙

- 이메일 인증 상태는 Redis로 관리합니다.
- `auth.tb_email_auth` 테이블은 현재 이력성 저장 용도입니다.

## Board APIs

### 게시판 분류

- `lol`
- `maple`
- `free`

카테고리 데이터 소스는 `board.tb_post_categories` 입니다.

### 1. 카테고리 조회

- Method: `GET`
- URI: `/api/board/categories`
- 인증: 불필요

Response `200`

```json
[
  {
    "id": 1,
    "categoryKey": "lol",
    "name": "리그오브레전드",
    "iconUrl": "https://drive.towardadiamond.com/tad/category-icons/lol.webp",
    "summary": "리그오브레전드 게시판",
    "displayOrder": 1
  }
]
```

### 2. 게시글 목록 조회

- Method: `GET`
- URI: `/api/board/posts`
- 인증: 불필요

Query

- `categoryKey`: 선택
- `postType`: 선택, `all | free | info`
- `page`: 선택, 기본 `0`
- `size`: 선택, 기본 `20`

Request Example

```http
GET /api/board/posts?categoryKey=lol&postType=free&page=0&size=20
```

Response `200`

```json
{
  "items": [
    {
      "id": 101,
      "categoryKey": "lol",
      "categoryName": "리그오브레전드",
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

규칙

- `postType` 값은 `free`, `info` 입니다.
- 정렬 기준은 `is_notice DESC`, `post_type ASC`, `created_at DESC` 입니다.

### 3. 게시글 상세 조회

- Method: `GET`
- URI: `/api/board/posts/{postId}`
- 인증: 불필요

Request Example

```http
GET /api/board/posts/101
```

Response `200`

```json
{
  "id": 101,
  "categoryId": 1,
  "categoryKey": "lol",
  "categoryName": "리그오브레전드",
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

규칙

- 상세 조회 시 `view_count` 가 1 증가합니다.

## Security Rules

- `/auth/**`: 공개
- `/board/**`: 공개
- 그 외 경로는 인증 필요

## DB Mapping Rules

- 인증 스키마: `auth`
- 게시판 스키마: `board`
- 엔티티에 스키마명을 명시합니다.

예시

```java
@Table(name = "tb_user", schema = "auth")
@Table(name = "tb_post", schema = "board")
```
