# TAD 웹 백엔드 Garage 설정

## 목적

파일 객체는 Garage S3 호환 API로 저장하고, 웹 클라이언트에 반환하는 공개 주소는 Drive 서비스의 공개 경로로 통일한다.

## 런타임 설정

배포 컨테이너에 다음 환경 변수를 제공한다. 실제 값은 배포 비밀값 또는 서버 설정에만 저장한다.

```text
MINIO_ENDPOINT=https://s3.example.com
MINIO_ACCESS_KEY=<garage-access-key>
MINIO_SECRET_KEY=<garage-secret-key>
MINIO_BUCKET=tad
MINIO_DRIVE_PUBLIC_URL=https://drive.example.com
```

`MINIO_DRIVE_PUBLIC_URL`이 설정되면 반환 URL은 다음 형식이다.

```text
https://drive.example.com/public/tad/{encoded-object-key}
```

## 권한 원칙

- 웹 백엔드용 Garage 키는 `tad` 버킷에만 읽기·쓰기 권한을 가진다.
- 버킷 생성이나 공개 정책 변경 권한은 주지 않는다.
- 공개 여부 검사는 Drive 서비스가 담당한다.

## 공개 URL base 우선순위

`MINIO_DRIVE_PUBLIC_URL`을 우선 사용한다. 이 값이 없으면 `MINIO_PUBLIC_URL`을 Drive 공개 URL base로 사용하며, 두 경우 모두 `/public/{bucket}/{objectKey}` 형식으로 반환한다.

S3 endpoint는 객체 저장 API 용도이므로 공개 URL 생성에 사용하지 않는다.
