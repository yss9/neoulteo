# Neoulteo

Vue 3 + Spring Boot 기반 여행 웹서비스입니다. 관광지 검색, 사용자 핫플레이스, 지도 기반 여행 코스 저장 기능을 제공합니다.

## 1. 프로젝트 처음 받기

처음 받는 사람은 저장소를 clone합니다.

```bash
git clone https://lab.ssafy.com/jrdtjr/neoulteo.git
cd neoulteo
```

이미 받은 사람은 최신 코드를 가져옵니다.

```bash
git checkout master
git pull origin master
```

## 2. Git에 올리지 않는 파일

아래 파일과 폴더는 Git에 올리지 않습니다.

```text
frontend/.env
frontend/node_modules/
frontend/dist/
backend/target/
storage/
.idea/
*.iml
```

직접 만들어야 하는 파일은 보통 `frontend/.env`입니다.

## 3. Git 작업 흐름

`master`에는 직접 작업하지 않고, 기능별 브랜치를 만들어 작업합니다.

작업 시작 전에는 항상 최신 `master`를 받습니다.

```bash
git checkout master
git pull origin master
```

새 브랜치를 만듭니다.

```bash
git checkout -b feature/작업이름
```

예시:

```bash
git checkout -b feature/community-board
git checkout -b feature/plan-title
git checkout -b fix/login-error
```

작업 후 변경 파일을 확인합니다.

```bash
git status
```

변경 파일을 스테이징합니다.

```bash
git add .
```

커밋합니다.

```bash
git commit -m "작업 내용"
```

브랜치를 원격 저장소에 올립니다.

```bash
git push -u origin feature/작업이름
```

GitLab에서 Merge Request를 만듭니다.

- Source branch: 내가 작업한 브랜치
- Target branch: `master`

팀원이 확인한 뒤 `master`로 merge합니다.

## 4. master에 직접 push하지 않기

팀 작업에서는 `master`에 직접 push하지 않는 것이 좋습니다.

권장 방식:

```text
브랜치 생성 -> 작업 -> commit -> push -> Merge Request -> 리뷰 -> master merge
```

## 5. 팀원이 merge한 코드 가져오기

팀원이 `master`에 코드를 합쳤다면 내 로컬에서도 최신 `master`를 받아야 합니다.

```bash
git checkout master
git pull origin master
```

내 작업 브랜치에 최신 `master` 내용을 반영하려면:

```bash
git checkout feature/내작업
git merge master
```

원격 기준으로 바로 반영하려면:

```bash
git fetch origin
git merge origin/master
```

## 6. 충돌이 났을 때

같은 파일의 같은 부분을 여러 사람이 수정하면 충돌이 날 수 있습니다.

충돌이 나면 파일 안에 아래처럼 표시됩니다.

```text
<<<<<<< HEAD
내 코드
=======
팀원 코드
>>>>>>> origin/master
```

직접 파일을 열고 최종 코드만 남깁니다.

예시:

```html
<button>수정</button>
```

아래 표시는 전부 삭제해야 합니다.

```text
<<<<<<<
=======
>>>>>>>
```

충돌을 고친 뒤:

```bash
git add .
git commit
```

## 7. MySQL DB 준비

현재 백엔드는 MySQL의 `ssafy_trip` 데이터베이스를 사용합니다.

기본 DB 설정은 [application.properties](backend/src/main/resources/application.properties)에 있습니다.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ssafy_trip?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=ssafy
spring.datasource.password=ssafy
```

MySQL에서 DB와 계정을 준비합니다.

```sql
CREATE DATABASE IF NOT EXISTS ssafy_trip DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'ssafy'@'localhost' IDENTIFIED BY 'ssafy';
GRANT ALL PRIVILEGES ON ssafy_trip.* TO 'ssafy'@'localhost';
FLUSH PRIVILEGES;
```

이미 `ssafy` 계정이 있으면 `CREATE USER`에서 오류가 날 수 있습니다. 그 경우 계정 생성은 건너뛰고 권한만 확인하면 됩니다.

## 8. DB 스키마 생성

최신 테이블 구조는 아래 파일에 정리되어 있습니다.

```text
backend/src/main/resources/schema.sql
docs/database/schema.sql
```

현재 설정은 아래처럼 되어 있습니다.

```properties
spring.sql.init.mode=always
```

그래서 백엔드를 실행하면 Spring Boot가 `backend/src/main/resources/schema.sql`을 자동으로 실행합니다.

새로 프로젝트를 받은 사람은 `ssafy_trip` DB만 만든 뒤 아래 명령으로 전체 스키마를 처음부터 생성하면 됩니다.

```bash
mysql -u ssafy -p ssafy_trip < backend/src/main/resources/schema.sql
```

새 DB에서 처음 시작하는 경우 추천 순서:

1. `ssafy_trip` DB 생성
2. `backend/src/main/resources/schema.sql` 실행
3. 관광지 dump SQL import
4. 백엔드 실행

백엔드를 먼저 실행해도 `schema.sql`은 자동 실행됩니다. 다만 관광지 dump를 넣기 전에 테이블을 확실히 만들어두려면 위 명령으로 스키마를 먼저 생성하는 방식이 가장 단순합니다.

## 9. 관광지 데이터 import

관광지 검색 기능을 사용하려면 저장소에 포함된 관광지 dump 데이터를 MySQL에 넣어야 합니다.

```bash
mysql -u ssafy -p ssafy_trip < docs/database/SSAFY_TRIP_Dump.sql
```

파일 위치는 `docs/database/SSAFY_TRIP_Dump.sql`입니다.

## 10. 개발용 핫플 데이터

개발용 핫플 데이터가 필요하면 아래 파일을 실행합니다.

```bash
mysql -u ssafy -p ssafy_trip < docs/dev-seed-hotplaces.sql
```

## 11. Spring Batch 메타 테이블

DB에 아래처럼 `batch_`로 시작하는 테이블이 보일 수 있습니다.

```text
batch_job_instance
batch_job_execution
batch_job_execution_params
batch_job_execution_context
batch_step_execution
batch_step_execution_context
batch_job_seq
batch_step_execution_seq
batch_job_execution_seq
```

이 테이블들은 Neoulteo 도메인 테이블이 아니라 Spring Batch 내부 실행 이력 관리용 테이블입니다.

현재 설정은 아래처럼 되어 있습니다.

```properties
spring.batch.jdbc.initialize-schema=always
spring.batch.job.enabled=false
```

그래서 백엔드 실행 시 Spring Batch 메타 테이블은 자동 생성될 수 있습니다. `schema.sql`에 직접 넣지 않아도 됩니다.

## 12. Kakao Map 키 설정

`frontend/.env.example`을 참고해서 `frontend/.env` 파일을 만듭니다.

```env
VITE_KAKAO_MAP_APP_KEY=본인_카카오_JavaScript_키
```

Kakao Developers에서 JavaScript 키를 사용해야 합니다.

Web platform domain에는 아래 주소를 등록합니다.

```text
http://localhost:5173
http://127.0.0.1:5173
```

`frontend/.env`는 Git에 올리지 않습니다.

## 13. Backend 실행

```bash
cd backend
.\mvnw.cmd spring-boot:run
```

백엔드 주소:

```text
http://localhost:8080/neoulteo
```

## 14. Frontend 실행

pnpm을 사용합니다.

```bash
cd frontend
pnpm install
pnpm dev
```

프론트엔드 주소:

```text
http://localhost:5173
```

## 15. 빌드 확인

Backend:

```bash
cd backend
.\mvnw.cmd -q -DskipTests package
```

Frontend:

```bash
cd frontend
pnpm build
```

## 16. 처음 실행 순서 요약

1. `git clone https://lab.ssafy.com/jrdtjr/neoulteo.git`
2. MySQL 실행
3. `ssafy_trip` DB와 `ssafy` 계정 준비
4. `mysql -u ssafy -p ssafy_trip < backend/src/main/resources/schema.sql`
5. `mysql -u ssafy -p ssafy_trip < docs/database/SSAFY_TRIP_Dump.sql`
6. `frontend/.env` 생성
7. 백엔드 실행
8. 프론트엔드에서 `pnpm install`
9. 프론트엔드에서 `pnpm dev`
10. 브라우저에서 `http://localhost:5173` 접속

## 17. 평소 작업 순서 요약

```bash
git checkout master
git pull origin master
git checkout -b feature/작업이름
```

작업 후:

```bash
git status
git add .
git commit -m "작업 내용"
git push -u origin feature/작업이름
```

그 다음 GitLab에서 Merge Request를 만들고 `master`로 합칩니다.

## 18. 주의

- `master`에 직접 push하지 않습니다.
- 기능마다 브랜치를 만들어 작업합니다.
- Merge Request 전에 로컬에서 백엔드/프론트 빌드를 확인합니다.
- `node_modules`, `dist`, `target`, `.idea`, `.env`는 Git에 올리지 않습니다.
- `package.json`, `pnpm-lock.yaml`, `pnpm-workspace.yaml`은 Git에 올라가는 것이 맞습니다.
- 현재 여행 코스 경로는 실제 도보/자동차 길찾기가 아니라 선택한 관광지 좌표를 직선으로 연결합니다.
- 실제 도로 경로는 나중에 Kakao Mobility Directions API를 백엔드에서 호출하는 방식으로 확장할 수 있습니다.

## 19. DB 스키마 변경 사항 (커뮤니티 기능 추가)

커뮤니티 기능을 위해 `posts` 테이블이 추가되었습니다.

```sql
CREATE TABLE IF NOT EXISTS posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category VARCHAR(20) NOT NULL, -- NOTICE, FREE, REVIEW, QNA, PLAN_SHARE
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    travel_plan_id VARCHAR(30), -- PLAN_SHARE일 경우 연동
    views INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_posts_travel_plan FOREIGN KEY (travel_plan_id) REFERENCES travel_plans (id) ON DELETE SET NULL
);
```

## 20. Spring AI Tool 모듈 작업 메모

현재 Spring AI 관련 코드는 새 프로젝트를 만든 것이 아니라 기존 백엔드 안에 `com.neoulteo.ai` 패키지로 추가했습니다.

현재 추가된 API:

- `POST /neoulteo/app/ai/chat`
- `POST /neoulteo/app/ai/smart-travel-chat`

현재 구성:

- `AttractionTool`: 기존 관광지 DB에서 지역, 키워드, 콘텐츠 타입 기준으로 관광지를 조회합니다.
- `WeatherTool`: 날씨 도구 구조만 만들어두었습니다.
- `TravelSearchTool`: 여행 행사/축제 검색 도구 구조만 만들어두었습니다.
- `AiTravelChatService`: Spring AI `ChatClient`를 사용해서 도구 결과를 바탕으로 여행 답변을 만듭니다.
- `NEOULTEO_AI_ENABLED=false`가 기본값이라 GMS 키가 없어도 서버는 실행됩니다.
- AI 기능을 실제 LLM으로 실행하려면 `NEOULTEO_AI_ENABLED=true`, `GMS_OPENAI_API_KEY` 설정이 필요합니다.

환경 변수 예시:

```env
NEOULTEO_AI_ENABLED=false
GMS_OPENAI_API_KEY=your-gms-openai-api-key
GMS_OPENAI_MODEL=gpt-4o-mini
WEATHER_API_KEY=your-weather-api-key
TRAVEL_SEARCH_API_KEY=your-search-api-key
TOUR_API_SERVICE_KEY=your-tour-api-service-key
```

`backend/src/main/resources/application-example.properties`에 예시 설정을 추가해두었습니다.

## 21. 주의 / 미완료 작업

아직 완성 기능이 아니라 구조를 먼저 잡아둔 부분:

- `WeatherTool`은 실제 날씨 API와 아직 연결되지 않았습니다. `WEATHER_API_KEY`가 없으면 안내 메시지만 반환합니다.
- `TravelSearchTool`은 외부 검색 API와 아직 연결되지 않았습니다. 현재는 DB의 축제/행사 데이터 조회를 우선 사용합니다.
- 프론트엔드 우측 하단 챗봇은 FastAPI AI 서버를 호출합니다. Spring AI와 FastAPI는 역할을 나누어 함께 사용합니다.
- 여행계획 AI 평가는 현재 프론트 규칙 기반입니다. 나중에 Spring AI Tool Calling으로 백엔드 평가 API를 만들 수 있습니다.
- 여행 코스 지도 경로는 아직 실제 도보/자동차 경로가 아니라 관광지 좌표를 직선으로 연결합니다.
- 실제 경로 안내는 Kakao Mobility Directions API 같은 길찾기 API를 백엔드에서 호출하는 방식으로 확장해야 합니다.
- Tour API 배치는 전체 데이터를 한 번에 계속 호출하면 API 제한에 걸릴 수 있으므로 콘텐츠 타입/지역/요일 단위로 나누어 실행하는 방식이 좋습니다.
- `TOUR_API_SERVICE_KEY`, `GMS_OPENAI_API_KEY` 같은 키는 Git에 올리지 말고 로컬 환경 변수나 개인 설정 파일로 관리합니다.

앞으로 하면 좋은 작업:

1. 날씨 API 실제 연동
2. 축제/행사 외부 검색 API 실제 연동
3. 여행계획 AI 평가를 백엔드 Spring AI API로 이전
4. Langfuse 같은 도구로 Tool Calling 흐름 모니터링
5. 실제 도보/자동차 경로 API 연동
6. 핫플레이스와 관광지 FK 구조를 더 명확하게 정리
7. 관리자 권한, 공지사항 작성 권한, 게시판 권한 검증 보강

## 22. AI 서버 역할 분리

Neoulteo는 AI 기능을 두 갈래로 사용합니다. 둘 다 GMS OpenAI 키를 기준으로 동작합니다.

### Spring AI

Spring AI는 Spring Boot 백엔드 안에서 서비스 DB와 비즈니스 로직을 사용하는 AI입니다.

사용 가능한 API:

- `POST /neoulteo/app/ai/chat`
- `POST /neoulteo/app/ai/smart-travel-chat`

사용 목적:

- 관광지 DB 조회 기반 추천
- 지역, 키워드, 콘텐츠 타입 기반 여행지 추천
- 나중에 여행계획 평가, 저장 장소 추천, 핫플레이스 추천 같은 내부 서비스 Tool Calling으로 확장

현재 프론트 화면에 직접 연결된 버튼은 없으며, API 테스트 도구나 별도 프론트 연동을 통해 호출할 수 있습니다.

예시:

```bash
curl -X POST http://localhost:8080/neoulteo/app/ai/smart-travel-chat \
  -H "Content-Type: application/json" \
  -d "{\"message\":\"부산 비 오는 날 갈만한 실내 관광지 추천해줘\"}"
```

Spring AI를 실제 LLM 응답으로 사용하려면 `backend/src/main/resources/application-local.properties` 또는 환경 변수에 다음 값을 둡니다.

```properties
NEOULTEO_AI_ENABLED=true
GMS_OPENAI_API_KEY=GMS인증키
GMS_OPENAI_MODEL=gpt-4o-mini
```

### FastAPI AI 서버

FastAPI는 문서 기반 RAG와 Python AI 실험을 담당합니다.

현재 프론트 우측 하단 `Neoulteo AI` 챗봇은 `frontend/src/api/aiApi.js`를 통해 FastAPI 서버를 호출합니다.

사용 가능한 API:

- `POST http://127.0.0.1:8000/chat`
- `POST http://127.0.0.1:8000/search`
- `POST http://127.0.0.1:8000/integrated-chat`
- `POST http://127.0.0.1:8000/upload`

사용 목적:

- PDF, TXT, MD 문서 업로드
- 업로드한 문서와 `ai-server/data` 폴더 문서 기반 RAG 질의응답
- ChromaDB, sentence-transformers 기반 벡터 검색
- 나중에 문서 요약, 관광 가이드 문서 검색, 이미지/OCR 같은 Python 중심 AI 기능 확장

처음 실행할 때는 Python 가상환경을 만든 뒤 그 안에서 패키지를 설치합니다.

```powershell
cd ai-server

python -m venv .venv
.\.venv\Scripts\Activate.ps1

python -m pip install --upgrade pip
pip install -r requirements.txt

$env:GMS_OPENAI_API_KEY="GMS인증키"
$env:GMS_OPENAI_BASE_URL="https://gms.ssafy.io/gmsapi/api.openai.com/v1"
$env:GMS_OPENAI_MODEL="gpt-4o-mini"

uvicorn main:app --reload --port 8000
```

실행 후 아래 주소에서 FastAPI 문서를 확인할 수 있습니다.

```text
http://127.0.0.1:8000/docs
```

두 번째 실행부터는 이미 `.venv`와 패키지가 있으므로 활성화 후 바로 실행하면 됩니다.

```powershell
cd ai-server
.\.venv\Scripts\Activate.ps1

$env:GMS_OPENAI_API_KEY="GMS인증키"
$env:GMS_OPENAI_BASE_URL="https://gms.ssafy.io/gmsapi/api.openai.com/v1"
$env:GMS_OPENAI_MODEL="gpt-4o-mini"

uvicorn main:app --reload --port 8000
```

FastAPI 서버가 실행되어 있어야 프론트 우측 하단 `Neoulteo AI` 챗봇이 정상 동작합니다.

로컬에서 생성되는 `.venv`, `__pycache__`, `ai-server/chroma_data`는 Git에 올리지 않습니다.

정리하면 Spring AI는 서비스 DB와 내부 기능을 사용하는 행동형 AI이고, FastAPI는 문서/RAG/벡터 검색을 담당하는 지식 검색형 AI입니다.
