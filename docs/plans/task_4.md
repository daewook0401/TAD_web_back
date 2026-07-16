# task_4 수행계획서: Garage S3 저장소 전환 및 MinIO 제거 준비

## 목표

TAD 웹 백엔드의 객체 저장소 클라이언트와 설정을 Garage S3로 전환하고, MinIO 의존성을 제거할 수 있는 상태를 만든다.

## 현재 현상 또는 요구사항

- 백엔드가 `minio.*` 설정, MinIO SDK, MinIO 명칭 클래스를 사용한다.
- 배포 환경에 Garage 전용 키가 준비돼 있다.

## 확인 방법

- 코드·의존성에서 MinIO 전용 클라이언트와 설정 키가 제거됐는지 검색한다.
- 표준 S3 클라이언트가 Garage path-style endpoint로 객체 업로드할 수 있는지 확인한다.
- 전체 Gradle 테스트와 bootJar 빌드를 실행한다.

## 영향 범위

- 저장소 설정·업로드 서비스
- 분석 이미지·게시판 첨부 업로드
- GitHub Actions 배포 설정

## 검증 방법

1. 단위 테스트
2. 전체 Gradle 테스트 및 bootJar 빌드
3. Garage S3 객체 업로드·읽기 확인
4. GitHub Actions 배포 확인

## 단계별 작업 개요

1. Java 저장소 구현을 표준 S3 API 기반 Garage 구현으로 전환한다.
2. 배포 환경에 Garage 설정을 안전하게 제공한다.
3. 테스트·배포 후 업로드 경로를 검증한다.

## 승인

작업지시자의 Garage 연동 전환 지시로 승인되었다.
