# Neoulteo (너울터) Project Guidance

이 파일은 프로젝트의 기술 표준, 아키텍처 가이드 및 개발 협업 규칙을 정의합니다.

## 1. Tech Stack
- **Backend:** Java 17, Spring Boot 3.3.5, MyBatis, Spring Security, Spring Batch
- **Frontend:** Vue 3 (Composition API), Vite, pnpm, Kakao Map API
- **AI Server:** Python, FastAPI (관광 데이터 처리 및 AI 추천)
- **Database:** MySQL (ssafy_trip)

## 2. Architecture & Design Patterns
### Backend
- **Layered Architecture:** `Controller` -> `Service` -> `DAO/Mapper` -> `MyBatis XML`
- **Batch Processing:** Spring Batch를 사용하여 공공 데이터 수집 및 AI 가공 처리 (`com.neoulteo.batch`)
- **Security:** Spring Security + RememberMe 기반 인증

### Frontend
- **Component-based:** `src/components` (재사용성), `src/pages` (라우트 단위)
- **State Management:** authStore (Pinia 스타일의 reactive state)
- **API Communication:** Axios 기반의 `src/api/http.js` 공통 모듈 사용

## 3. Coding Conventions
- **Naming:** 
  - Backend: PascalCase (Classes), camelCase (Methods, Variables)
  - Frontend: PascalCase (Components), camelCase (Variables)
- **MyBatis:** XML 매퍼 파일은 `src/main/resources/mappers`에 위치하며, ID는 DAO의 메서드 명과 일치시킬 것.
- **Error Handling:** 글로벌 예외 처리를 지향하며, API 응답은 일관된 형식을 유지할 것.

## 4. Key Directories
- `backend/src/main/java/com/neoulteo/batch`: 투어 데이터 배치 작업 로직
- `ai-server/`: 관광지 데이터 분석 및 AI 모델링 서버
- `frontend/src/api/`: 백엔드/AI 서버와의 통신 규격 정의
- `docs/database/`: DB 스키마 및 마이그레이션 스크립트

## 5. Agent Instructions (Special Mandates)
- **Batch 작업 수정 시:** `TourBatchJobConfig`의 흐름과 `TourBatchRuntimeContext`를 반드시 먼저 파악할 것.
- **지도 기능 수정 시:** `KakaoMap.vue`와 `koreaSidoPaths.js`의 의존성을 확인할 것.
- **SQL 수정 시:** `docs/database/schema.sql`과 MyBatis XML 파일 간의 동기화를 반드시 유지할 것.
- **AI 연동 시:** `ai-server`의 `main.py` 엔드포인트와 프론트엔드 `aiApi.js` 간의 규격을 확인할 것.
