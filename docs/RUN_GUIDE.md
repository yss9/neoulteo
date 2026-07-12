# Neoulteo 실행 가이드

이 문서는 Neoulteo 프로젝트를 처음 받은 사람이 로컬에서 실행하기 위한 설정을 정리한 문서입니다.

## 1. 필요 프로그램

- Java 17
- MySQL 8.x
- Node.js 20 이상
- pnpm
- Python 3.11 이상
- Git

pnpm이 없다면 한 번만 설치합니다.

```powershell
npm install -g pnpm
```

## 2. 프로젝트 받기

```powershell
git clone https://github.com/yss9/neoulteo.git
cd neoulteo
```

## 3. MySQL DB 생성

MySQL에 접속합니다.

```powershell
mysql -u root -p
```

아래 SQL을 실행합니다.

```sql
CREATE DATABASE IF NOT EXISTS ssafy_trip
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'ssafy'@'localhost' IDENTIFIED BY 'ssafy';
GRANT ALL PRIVILEGES ON ssafy_trip.* TO 'ssafy'@'localhost';
FLUSH PRIVILEGES;
```

기본 백엔드 DB 설정은 다음과 같습니다.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ssafy_trip?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=ssafy
spring.datasource.password=ssafy
```

## 4. DB 스키마와 관광지 데이터 넣기

스키마를 먼저 생성합니다.

```powershell
mysql -u ssafy -p ssafy_trip < backend/src/main/resources/schema.sql
```

관광지 dump 데이터를 넣습니다.

```powershell
mysql -u ssafy -p ssafy_trip < docs/database/SSAFY_TRIP_Dump.sql
```

개발용 핫플레이스 샘플 데이터가 필요하면 아래 파일을 추가로 실행합니다.

```powershell
mysql -u ssafy -p ssafy_trip < docs/dev-seed-hotplaces.sql
```

Spring Boot 실행 시 `spring.sql.init.mode=always` 설정으로 `schema.sql`, `data.sql`이 실행됩니다. 이미 데이터가 있는 DB에서 실행할 때는 중복 데이터가 생기지 않도록 현재 DB 상태를 확인하세요.

## 5. 백엔드 로컬 설정

로컬 개인 키는 Git에 올리지 않는 `application-local.properties`에 넣습니다.

파일 위치:

```text
backend/src/main/resources/application-local.properties
```

예시:

```properties
TOUR_API_SERVICE_KEY=본인_TourAPI_서비스키
GMS_OPENAI_API_KEY=본인_GMS_OpenAI_키
NEOULTEO_AI_ENABLED=true
TMAP_APP_KEY=본인_TMAP_APP_KEY
COMMUNITY_ADMIN_EMAILS=a@a
```

키가 없어도 기본 기능은 실행할 수 있지만, 아래 기능은 제한됩니다.

| 키 | 필요한 기능 |
| --- | --- |
| `TOUR_API_SERVICE_KEY` | TourAPI 배치 동기화 |
| `GMS_OPENAI_API_KEY` | Spring AI, AI 코스 평가, 배치 AI 요약 |
| `TMAP_APP_KEY` | 도보/자동차 경로 표시 |
| `WEATHER_API_KEY` | 날씨 Tool 확장 |
| `TRAVEL_SEARCH_API_KEY` | 외부 여행 검색 Tool 확장 |

## 6. 백엔드 실행

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

기본 주소:

```text
http://localhost:8080/neoulteo
```

## 7. 프론트엔드 설정

프론트엔드에서 Kakao Map을 사용하려면 `.env` 파일을 만듭니다.

파일 위치:

```text
frontend/.env
```

예시:

```env
VITE_KAKAO_MAP_APP_KEY=본인_카카오_JavaScript_키
```

Kakao Developers에서 Web 플랫폼 도메인에 아래 주소를 등록합니다.

```text
http://localhost:5173
http://127.0.0.1:5173
```

## 8. 프론트엔드 실행

```powershell
cd frontend
pnpm install
pnpm dev
```

기본 주소:

```text
http://localhost:5173
```

## 9. FastAPI RAG 서버 실행

FastAPI 서버는 Spring AI와 별도로 문서 기반 RAG 테스트에 사용합니다.

```powershell
cd ai-server
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
$env:GMS_OPENAI_API_KEY="본인_GMS_OpenAI_키"
python -m uvicorn main:app --reload --port 8000
```

종료는 실행 중인 터미널에서 `Ctrl + C`를 누릅니다.

## 10. Spring Batch 수동 실행

백엔드 서버가 켜진 상태에서 TourAPI 동기화 배치를 실행할 수 있습니다.

```powershell
curl -X POST http://localhost:8080/neoulteo/api/batch/tour
```

배치 결과 PDF는 기본적으로 아래 경로에 생성됩니다.

```text
storage/output
```

이 폴더는 실행 결과물이므로 Git에 올리지 않습니다.

## 11. 자주 생기는 문제

### Kakao Map이 안 뜨는 경우

- `frontend/.env`에 `VITE_KAKAO_MAP_APP_KEY`가 있는지 확인합니다.
- Kakao Developers에 `localhost:5173`, `127.0.0.1:5173` 도메인이 등록되어 있는지 확인합니다.
- 프론트엔드 서버를 재시작합니다.

### AI 기능에서 Request failed가 뜨는 경우

- `GMS_OPENAI_API_KEY`가 설정되어 있는지 확인합니다.
- `NEOULTEO_AI_ENABLED=true` 설정이 있는지 확인합니다.
- 백엔드를 재시작합니다.

### DB 연결이 안 되는 경우

- MySQL 서버가 켜져 있는지 확인합니다.
- `ssafy_trip` DB가 생성되어 있는지 확인합니다.
- `ssafy / ssafy` 계정이 존재하는지 확인합니다.

### 프론트엔드 패키지 설치가 안 되는 경우

```powershell
cd frontend
pnpm install
```

그래도 안 되면 `node_modules`를 지운 뒤 다시 설치합니다.

```powershell
Remove-Item -LiteralPath node_modules -Recurse -Force
pnpm install
```

## 12. Git에 올리지 않는 파일

아래 파일과 폴더는 개인 환경 또는 실행 결과물이므로 Git에 올리지 않습니다.

```text
frontend/.env
backend/src/main/resources/application-local.properties
ai-server/.env
node_modules/
.venv/
target/
dist/
storage/
```
