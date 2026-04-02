# '제주 온기' API 명세서

> **버전:** 0.10 (해커톤 초안)  
> **Base URL:** `https://api/v1` (배포 시 교체)  
> **포맷:** JSON (`Content-Type: application/json`)  
> **인증:** **로그인 없음.** 일반 유저는 **닉네임만**으로 헌화 메시지를 남길 수 있으며, 닉네임은 **중복 허용**. 시니어(어르신)도 로그인 없이 **이름 입력 → 지역(유적지) 선택 → 음성 녹음** 순서의 클라이언트 플로우에 맞춰, 업로드 요청에 이름·`siteId`·음성 파일을 함께 보낸다.

---

## 1. 개요

| 도메인 | 설명 |
|--------|------|
| 사용자 | **로그인 없음.** 일반: 헌화 시 `nickname`(중복 가능). 시니어: 이름 입력 → `siteId` 지역 선택 → 음성 업로드. |
| 유적지·콘텐츠 | 지도 핑(약 5곳), 순차 해금, 사진·설명·TTS 청취 |
| 청취·진행 | 핑별 청취 완료 처리, 다음 핑 해금 |
| 헌화·적립 | 핑(유적지) 1개 완료마다 **동백 꽃잎 1개**가 쌓인다. 모든 핑을 완료하면 마지막 화면에서 **헌화 메시지 1회**를 남긴다. **유저 결제 없음.** 꽃잎 1개당 **1,000원 상당**을 시스템에서만 합산(포인트·적립액 형태). 실제 기부 실행은 **제휴 기업 등 사회적 약속**으로 별도 처리. |
| 집계 | 참여(헌화) 건수, 동백 총 송이, **적립 합계 금액(원)** — 모두 위 규칙으로 산출. **실시간 불필요**, 서버는 **약 10분마다** 스냅샷 갱신(캐시·배치 등)을 권장. |
| 시니어 기록 | 음성 업로드 → STT → LLM 정제 → DB 저장 → TTS 생성(비동기) |
| 메시지 피드 | 전체 사용자 헌화 메시지 목록 |

---

## 2. 공통 규칙

### 2.1 HTTP 상태 코드

| 코드 | 의미 |
|------|------|
| `200` | 성공 (GET, PUT, PATCH) |
| `201` | 생성 성공 (POST) |
| `204` | 본문 없음 성공 (DELETE 등) |
| `400` | 잘못된 요청 (검증 실패) |
| `401` | 미인증 (본 명세에서는 로그인 미사용. 향후 관리자 API 등 추가 시 사용) |
| `403` | 권한 없음 (순서 위반 등) |
| `404` | 리소스 없음 |
| `409` | 충돌 (동일 `idempotencyKey` 재처리 등 비즈니스 규칙) |
| `422` | 처리 불가 (STT 실패 등) |
| `500` | 서버 오류 |

### 2.2 에러 응답 본문

```json
{
  "error": {
    "code": "SITE_LOCKED",
    "message": "이전 유적지 청취를 완료해야 합니다.",
    "details": {}
  }
}
```

### 2.3 페이지네이션 (목록 API)

쿼리: `page` (기본 1), `size` (기본 20, 최대 100).  
응답 예:

```json
{
  "items": [],
  "page": 1,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

### 2.4 클라이언트 식별 (순차 핑·시니어 업로드, 로그인 아님)

로그인은 사용하지 않는다.

- **지도 핑(진행/잠금):** (현재) 청취 순서·해금은 **클라이언트**에서 처리한다. `GET /sites`·`GET /sites/{id}`는 세션 없이 호출한다. 헌화·업로드 등 다른 API는 필요 시 **`X-Session-Id: <uuid>`** 로 익명 세션을 식별한다.
- **진행 리셋:** 플로우 마지막에서 “맨 처음으로 돌아가기”를 누르면 진행이 초기화된다. 서버는 `X-Session-Id` 기준으로 **회차(`resetVersion`)** 를 증가시키고, 이후 진행 판단은 해당 회차 기준으로 수행한다.
- **시니어 음성 업로드:** 동일하게 **`X-Session-Id`** 를 쓸 수 있다. 업로드 시 서버에 전달하면 기록과 연결해 두고, §7.0·`GET /contributions`(선택)에서 활용한다.

---

## 3. 유적지(Site)

지역 **이름·사진·관광 안내 음성** 등은 클라이언트 정적 데이터로 둔다. 백엔드는 **시니어 기록에 붙는 `siteId`·순서**와 **해당 지역의 게시된 시니어 스토리(무작위 1건)** 만 내려주며, **지역별 청취 완료**는 `POST /sites/{siteId}/complete-listen`으로 `siteId`(DB와 동일 UUID)·`X-Session-Id` 기준으로 저장한다(§4).

### 3.1 유적지 목록 조회 (식별자)

**`GET /sites`**

`order` 오름차순 정렬. 항목은 **`id`**, **`order`** 만 포함한다. (시니어 업로드·지도 핀 매핑용.)

**응답 `200`**

```json
{
  "sites": [
    {
      "id": "uuid",
      "order": 1
    }
  ]
}
```

### 3.2 유적지 상세 (시니어 스토리)

**`GET /sites/{siteId}`**

헤더·세션 없이 조회 가능.

**응답 `200`**

```json
{
  "id": "uuid",
  "order": 1,
  "elderStory": {
    "audioUrl": "/contributions/uuid/audio"
  }
}
```

- `elderStory`: 해당 지역에 `PUBLISHED` 시니어 기록이 여러 건이면 **서버에서 무작위 1건** 선택. 없으면 `null`.

---

## 4. 청취 진행 (순차 핑)

### 4.1 청취 완료 표시

**`POST /sites/{siteId}/complete-listen`**

요청 헤더(필수): `X-Session-Id: <uuid>`

본문: 선택적으로 `{ "durationListenedSec": 90 }` (분석·어뷰징 완화용).

서버는 **`listen_completions`** 에 `(세션, siteId, resetVersion)` 단위로 완료를 기록한다. `siteId`는 **`GET /sites`와 동일한 DB 식별자**여야 한다. **`order`가 더 작은 유적지**를 아직 완료하지 않았으면 **`403` (`SITE_LOCKED`)** — 이전 순서 핑을 먼저 완료해야 한다. 동일 `(세션, siteId, 회차)`로 재호출하면 멱등적으로 동일 형태로 성공한다.

**성공 `200`**

```json
{
  "siteId": "uuid",
  "listenCompleted": true,
  "nextSiteId": "uuid-or-null"
}
```

- 마지막 유적지면 `nextSiteId`는 `null`.
- 한 지역에서 안내 음성·시니어 스토리를 모두 들었을 때 클라이언트가 **한 번** 호출하는 것을 권장(세부는 프론트).

**`403`** — 이전 순서 유적지 미완료(`SITE_LOCKED`).

---

## 5. 마지막 플로우 후기(헌화)·동백 적립 (결제·PG 없음)

일반 유저는 **계정·로그인 없이** 전 지역 청취를 마친 뒤 마지막 화면에서 **후기(헌화) 메시지**를 남긴다. 표시 이름은 **`nickname`** 한 필드만 사용한다. **동일 닉네임 다수 허용**(UNIQUE 제약 없음).

API 경로: **`POST /tributes`** · **`POST /reviews`** 는 동일(후기 작성). **`GET /tributes`** · **`GET /reviews`** 는 동일(공개 피드).

### 5.0 적립 규칙 (시스템)

- **핑 1개 완료 = 동백 꽃잎 1개 = 1,000원 상당**으로 서버가 기록한다.
- 사용자에게는 **카드 결제·PG·포트원 등 결제 연동이 없다.**
- 화면에 보이는 “모인 기부금”은 **위 환산 규칙으로 계산된 합계(가상 적립액)**이며, 실제 자금 이전은 **제휴 기업·사회적 기부 약속** 등 기획 스토리에 맡긴다.

### 5.1 후기(헌화) 생성 (즉시 반영)

**`POST /tributes`** 또는 **`POST /reviews`**

요청 헤더(필수): `X-Session-Id: <uuid>`

```json
{
  "message": "평화를 빕니다 (최대 N자)",
  "nickname": "제주사랑",
  "idempotencyKey": "uuid-client-generated"
}
```

- `nickname`: 필수, 앞뒤 공백 제거 후 **1~50자**. **중복 가능.**
- `message`: 필수, 앞뒤 공백 제거 후 **1~500자**.
- `idempotencyKey`: (권장) 동일 키로 재요청 시 **기존 헌화 1건만** 유지하고 중복 적립 방지.
- 사전조건: 해당 세션의 **현재 회차(`resetVersion`)** 에서 **모든 유적지에 대해** `POST /sites/{siteId}/complete-listen` 완료 기록이 있어야 한다(§4). 미완료 시 **`403` (`TRIBUTE_NOT_ALLOWED_YET`)**.

**응답 `201`**

```json
{
  "tributeId": "uuid",
  "camelliaCount": 5,
  "pledgedAmountWon": 5000,
  "createdAt": "2026-04-02T12:00:00+09:00"
}
```

- `camelliaCount`: 이번 회차에서 완료한 핑 수(=모은 꽃잎 개수).
- `pledgedAmountWon`: 이번 요청으로 시스템에 **적립된 원화 상당액**. 원칙적으로 `camelliaCount × 1000` 과 일치.

### 5.2 후기(헌화) 공개 목록

**`GET /tributes`** 또는 **`GET /reviews`**

쿼리: `page`, `size`.

**응답 `200`** — 페이지네이션 + 항목 예:

```json
{
  "items": [
    {
      "id": "uuid",
      "message": "...",
      "nickname": "제주사랑",
      "camelliaCount": 5,
      "createdAt": "2026-04-02T12:00:00+09:00"
    }
  ],
  "page": 1,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

---

## 5.3 진행 리셋 (맨 처음으로 돌아가기)

**`POST /progress/reset`**

요청 헤더(필수): `X-Session-Id: <uuid>`

설명: 해당 세션의 진행 회차를 증가시켜(리셋) 이후 `unlocked/listenCompleted` 및 마지막 헌화 조건을 새 회차 기준으로 판단한다.

**응답 `200`**

```json
{
  "resetVersion": 1
}
```

---

## 6. 집계 (대시보드 카피용)

### 6.1 갱신 정책 (비실시간)

- 통계는 **실시간일 필요 없음.** 집계 값은 **최대 약 10분간 이전 스냅샷과 동일**할 수 있다.
- 구현 권장: 주기적 배치 집계, 또는 DB 집계 결과를 **TTL 약 600초**로 서버에 캐시하는 방식 등, 동일 효과이면 무엇이든 가능.
- 응답의 **`updatedAt`** 은 “이 수치가 마지막으로 집계·반영된 시각”이다(실시간 트랜잭션 시각 아님).
- HTTP 헤더(권장): `Cache-Control: public, max-age=600` — CDN·브라우저가 불필요한 재요청을 줄이도록.
- 클라이언트: 메인 화면에서 **폴링 주기는 10분 이상**이면 충분하다. 더 잦은 호출은 불필요.

### 6.2 공개 통계

**`GET /stats`**

**응답 `200`**

```json
{
  "participantCount": 1234,
  "camelliaTotal": 1234,
  "pledgedAmountTotalWon": 1234000,
  "pledgeUnitWon": 1000,
  "currency": "KRW",
  "refreshIntervalSec": 600,
  "updatedAt": "2026-04-02T12:00:00+09:00"
}
```

- `participantCount`: **헌화(동백 보내기) 완료 건수** = 참여 횟수. 닉네임 중복이 있으므로 고유 인원 수와는 다를 수 있다.
- `camelliaTotal`: 누적 동백(꽃잎) 합. 원칙적으로 “마지막 헌화”의 `camelliaCount` 합과 일치.
- `pledgedAmountTotalWon`: **시스템 적립 합계(원)**. 원칙적으로 `camelliaTotal × pledgeUnitWon` 과 일치.
- `pledgeUnitWon`: 동백 1송이당 환산 원액(기본 **1000**). 클라이언트 안내 문구에 사용 가능.
- `refreshIntervalSec`: 서버가 약속하는 **통계 갱신 간격(초)**. 기본 **600**(10분). UI에서 “최근 집계 기준” 안내에 활용 가능.
- `updatedAt`: 해당 스냅샷이 집계된 시각(§6.1 참고).

---

## 7. 시니어: 음성 기록 파이프라인

**로그인 없음.** 클라이언트 UX는 아래 순서로 진행한다.

1. **이름 입력** — 화면에 표시될 호칭(예: 김○○ 삼춘).
2. **지역 선택** — “어디 지역의 이야기를 들려주고 싶으세요?”에 대응하는 **`siteId`** 선택 (`GET /sites` 목록과 동일 ID).  
3. **음성 녹음** — 완료 후 아래 API로 업로드.

서버는 한 번의 업로드로 이름·지역·음성을 받는다(별도 “이름 등록 API”는 필수 아님).

### 7.0 처리 상태 추적 (로그인 없음)

로그인이 없으므로, 업로드 직후 **브라우저를 새로고침**하면 URL·메모리에만 있던 `contributionId`를 잃기 쉽다. 아래 **한 가지 이상**을 명세에 따른다.

1. **클라이언트 권장:** `POST /contributions/audio` 응답의 **`contributionId`** 를 **Web Storage(`localStorage` 등)** 에 저장한다. 이후 같은 브라우저에서는 `GET /contributions/{contributionId}` 로 처리 상태(§7.2)를 조회할 수 있다. (여러 건이면 배열로 누적 저장 가능.)
2. **서버 연동(선택):** 업로드·조회 시 동일한 **`X-Session-Id`** 헤더를 보내도록 하고, 서버는 해당 기록을 세션과 연결해 저장한다. (해커톤 범위)

### 7.1 녹음 업로드 (최대 약 2분)

**`POST /contributions/audio`**

**요청 헤더 (선택):** `X-Session-Id: <uuid>` — §7.0 항목 2. 서버는 동일 ID로 연결된 기록을 조회할 수 있게 저장하는 것을 권장.

`multipart/form-data`:

| 필드 | 타입 | 설명 |
|------|------|------|
| `contributorName` | string | 시니어가 입력한 이름(일반 유저 화면 `elderStory.contributorLabel` 등에 사용) |
| `siteId` | string | 이야기할 **지역(유적지)** ID |
| `audio` | file | wav/mp3/m4a 등 |
| `durationSec` | number | 클라이언트 측 녹음 길이 (검증용) |

**응답 `201`**

```json
{
  "contributionId": "uuid",
  "status": "QUEUED",
  "siteId": "uuid",
  "contributorName": "김○○"
}
```

**`400`** — 120초 초과, `contributorName`/`siteId` 누락 등.

### 7.2 처리 상태 조회 (폴링용)

**`GET /contributions/{contributionId}`**

**요청 헤더 (선택):** `X-Session-Id` — §7.0 항목 2와 연동 시 검증·로깅에 사용 가능.

**응답 `200`**

```json
{
  "id": "uuid",
  "siteId": "uuid",
  "contributorName": "김○○",
  "status": "QUEUED | PROCESSING | PUBLISHED | FAILED",
  "rawTranscript": "STT로 처리된 원문 (PUBLISHED 이후에도 동일 기록에 남음)",
  "correctedText": null,
  "ttsAudioUrl": null,
  "error": null
}
```

**현재 구현 기준 파이프라인 (원본 음성 청취):**

1. 업로드 → `QUEUED` (서버에 원본 파일 저장, `rawAudioUrl`).
2. 워커가 **STT**(예: CLOVA) → 인식 문장을 **`rawTranscript`로 DB 저장** → 곧바로 **`PUBLISHED`**.
3. 일반 유저는 `GET /sites/{siteId}`의 `elderStory.audioUrl`(예: `/contributions/{id}/audio`)로 **업로드한 원본 녹음**을 재생한다.
4. (향후) LLM 정제·TTS를 넣을 경우 `STT_DONE`·`CORRECTED`·`TTS_DONE` 등 상태와 `ttsAudioUrl`을 확장할 수 있다.

### 7.2a 원본 음성 스트리밍 (재생)

**`GET /contributions/{contributionId}/audio`**

- **`PUBLISHED`** 인 기록만 `200` 및 오디오 바이너리(`Content-Type`은 파일에 따라 자동 추론).
- 미게시·없음 → **`404`** (존재하지 않는 리소스로 응답).

클라이언트는 API 베이스 URL과 위 경로를 이어 전체 URL을 구성한다.

**재생 속도 1.25배** 등은 클라이언트 플레이어에서 처리.


### 7.3 (선택) 시니어 기록 목록·승인

해커톤 범위를 넘어 관리자 검수가 필요하면:

- **`GET /admin/contributions`** — `PENDING` 목록  
- **`PATCH /admin/contributions/{id}`** — `{ "status": "PUBLISHED" | "REJECTED" }`

---

## 8. 인증

| 구분 | 정책 |
|------|------|
| 일반 유저 | **로그인 없음.** 헌화 시 `nickname`만 전달(중복 가능). |
| 시니어 | **로그인 없음.** `contributorName` + `siteId` + 음성 파일로 기록. 처리 상태 조회는 §7.0(`contributionId` 저장 또는 `X-Session-Id`). |
| 순차 핑 진행 | (선택) 익명 `X-Session-Id` 등으로 서버에 청취 완료 상태 저장. |
| 관리자·검수 | 필요 시 별도 Bearer 토큰 등으로 `7.3`만 보호. |

---

## 9. 실시간 연동 (미사용)

실시간 푸시·양방향 소켓 연동은 **사용하지 않는다.** 통계는 `GET /stats` 를 **10분 단위** 등 여유 있는 주기로 호출하면 된다(§6.1 참고).

---

## 10. 데이터 모델 요약

| 엔티티 | 주요 필드 |
|--------|-----------|
| `Site` | id, order, name, thumbnailUrl, shortDescription, imageUrl, descriptionText, narrationAudioUrl |
| `AnonymousSession` | sessionId, resetVersion, createdAt, lastSeenAt |
| `ListenCompletion` | id, sessionId, siteId, resetVersion, completedAt, durationListenedSec |
| `FinalTribute` | id, sessionId, resetVersion, nickname(중복 가능), message, camelliaCount, pledgedAmountWon, idempotencyKey, createdAt |
| `ElderContribution` | id, siteId, (sessionId), contributorName, rawAudioUrl, rawTranscript, correctedText, ttsAudioUrl, status |

---

## 11. 버전·변경 이력

| 날짜 | 내용 |
|------|------|
| 2026-04-02 | 초안 작성 (기획서 4.3 온기보내기 반영) |
| 2026-04-02 | v0.2: 로그인 제거. 헌화 `nickname`(중복 허용). 시니어 이름·지역·녹음 플로우 및 `contributorName` 반영. |
| 2026-04-02 | v0.3: 유저 결제·PG·웹훅 제거. 동백 1송이당 1,000원 **시스템 적립**만. `GET /stats` 필드명 정리(`participantCount`, `pledgedAmountTotalWon` 등). |
| 2026-04-02 | v0.4: 통계 **비실시간**. 약 **10분** 갱신·캐시 정책, `refreshIntervalSec`, `Cache-Control` 권장. WebSocket 통계 제거. |
| 2026-04-02 | v0.5: 시니어 업로드 후 **처리 상태 추적** — §7.0 `localStorage` 권장, 선택 `X-Session-Id`·§7.1a `GET /contributions`. §2.4 보강. |
| 2026-04-02 | v0.6: 좌표 제거. 진행에 `X-Session-Id` 필수화 및 회차(`resetVersion`) 도입. 핑 완료로 꽃잎 적립 후 마지막에만 헌화 메시지 생성. `Tribute(siteId)` → `FinalTribute(sessionId, resetVersion)` 개념으로 변경. |
| 2026-04-03 | v0.8: `GET /sites`·상세 응답 슬림화(식별자+시니어 스토리). 시니어 파이프라인 STT→DB→즉시 `PUBLISHED`, 청취는 `GET /contributions/{id}/audio` 원본 스트리밍. 업로드 `201`. |
| 2026-04-03 | v0.9: `POST /sites/{siteId}/complete-listen`으로 지역별 청취 완료 DB 저장(순차 `order` 검증). `POST /tributes`는 동일 회차에서 전 지역 완료 후에만 허용. |
| 2026-04-03 | v0.10: 후기 API 별칭 `GET/POST /reviews`, 후기 생성 응답 `201`, 닉네임·메시지 길이 검증(50·500자). |
