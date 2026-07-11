# Neoulteo Spring AI RAG + Tool Calling

## 1. 목적

Neoulteo의 Spring AI는 단순 챗봇이 아니라, 백엔드가 가진 관광지 DB와 서비스 로직을 LLM이 활용하게 만드는 역할이다.

이번 구조는 다음 흐름을 가진다.

```text
사용자 질문
-> Spring Boot Controller
-> RAG 검색
-> Tool 실행
-> RAG 문서 + Tool 결과를 프롬프트에 주입
-> GMS OpenAI 모델 응답 생성
-> Vue 챗봇 표시
```

## 2. 어떤 API를 신청해야 하나

필수로 필요한 것은 SSAFY GMS OpenAI 키다.

신청/확인해야 하는 것:

- GMS OpenAI API Key
- Chat Completions 사용 가능 여부
- Embeddings 사용 가능 여부

로컬 설정 예시:

```properties
NEOULTEO_AI_ENABLED=true
GMS_OPENAI_API_KEY=발급받은_GMS_키
GMS_OPENAI_MODEL=gpt-4o-mini
GMS_OPENAI_EMBEDDING_MODEL=text-embedding-3-small
```

선택으로 있으면 좋은 API:

- OpenWeatherMap 같은 날씨 API: `WeatherTool` 실제 연동용
- 외부 검색 API: `TravelSearchTool`을 웹 검색 기반으로 확장할 때 사용
- TourAPI: 관광지 DB 배치 갱신용이며, RAG 챗봇 자체 필수 키는 아니다.

현재는 날씨/외부 검색 키가 없어도 서버가 죽지 않는다. 해당 Tool은 안내 메시지나 DB 기반 결과를 반환한다.

## 3. 구현된 API

Spring AI 관련 엔드포인트:

```text
POST /neoulteo/app/ai/chat
POST /neoulteo/app/ai/smart-travel-chat
POST /neoulteo/app/ai/evaluate-plan
POST /neoulteo/app/ai/travel-assistant
```

이번에 추가된 핵심 API:

```http
POST /neoulteo/app/ai/travel-assistant
Content-Type: application/json

{
  "message": "부산에서 비 오는 날 갈만한 실내 관광지 추천해줘",
  "useRag": true,
  "useTools": true
}
```

응답 형태:

```json
{
  "success": true,
  "message": "응답 생성 성공",
  "data": {
    "answer": "...",
    "ragSources": [],
    "toolResults": []
  }
}
```

## 4. RAG 구조

RAG는 `attractions` 테이블의 관광지 데이터를 참고 문서처럼 사용한다.

관련 코드:

- `backend/src/main/java/com/neoulteo/ai/service/RagSearchService.java`
- `backend/src/main/java/com/neoulteo/ai/resource/RagAttractionDocument.java`
- `backend/src/main/java/com/neoulteo/ai/resource/RagSourceResource.java`
- `backend/src/main/resources/mappers/AttractionMapper.xml`

동작 방식:

1. `AttractionMapper.findRagAttractionDocuments()`가 관광지 문서 후보를 가져온다.
2. Spring AI가 활성화되어 있고 EmbeddingModel이 있으면 문서와 질문을 임베딩한다.
3. 코사인 유사도로 관련 문서를 상위 5개 뽑는다.
4. 임베딩 호출이 실패하거나 비활성화되어 있으면 키워드 기반 검색으로 fallback한다.
5. 검색된 문서를 LLM 프롬프트에 근거 자료로 넣는다.

현재는 별도 Vector DB를 붙이지 않고 인메모리 인덱스로 시작한다. 나중에 데이터가 커지면 PostgreSQL pgvector, Redis Vector, Elasticsearch, Milvus 같은 Vector Store로 분리하면 된다.

## 5. Tool Calling 구조

현재 Tool은 LLM이 직접 자동 호출하는 방식이 아니라, 서버가 질문을 해석해서 필요한 Tool을 호출한 뒤 결과를 LLM에게 전달하는 방식이다.

관련 코드:

- `backend/src/main/java/com/neoulteo/ai/tool/AttractionTool.java`
- `backend/src/main/java/com/neoulteo/ai/tool/WeatherTool.java`
- `backend/src/main/java/com/neoulteo/ai/tool/TravelSearchTool.java`
- `backend/src/main/java/com/neoulteo/ai/service/AiTravelChatService.java`

현재 Tool 역할:

- `AttractionTool`: 기존 관광지 DB에서 지역/키워드/콘텐츠 타입 기반 관광지 조회
- `WeatherTool`: 날씨 API 연동 확장 포인트
- `TravelSearchTool`: 축제/행사/최신 여행 정보 확장 포인트

질문에 `날씨`, `비`, `눈`, `더워`, `추워`가 있으면 WeatherTool을 시도한다.

질문에 `축제`, `행사`, `이벤트`, `최신`이 있으면 TravelSearchTool을 시도한다.

AttractionTool은 기본으로 실행해서 관광지 DB 결과를 함께 제공한다.

## 6. Vue 챗봇 연결

프론트 챗봇은 기본적으로 Spring AI RAG+Tool 모드를 사용한다.

관련 코드:

- `frontend/src/components/Chatbot.vue`
- `frontend/src/api/aiApi.js`

챗봇 옵션:

- `Spring AI RAG+Tool` 체크됨: Spring Boot `/app/ai/travel-assistant` 호출
- 체크 해제: 기존 FastAPI `/chat` 또는 `/integrated-chat` 호출

이렇게 분리한 이유는 Spring AI와 FastAPI의 목적이 다르기 때문이다.

- Spring AI: DB, 서비스 로직, 여행계획 평가, Tool Calling 중심
- FastAPI: 문서 업로드, 벡터 검색, Python AI 생태계 중심

## 7. 실행 방법

백엔드:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

프론트엔드:

```powershell
cd frontend
pnpm install
pnpm dev
```

Spring AI를 실제 LLM으로 켜려면 `backend/src/main/resources/application-local.properties`에 다음 값을 넣는다.

```properties
NEOULTEO_AI_ENABLED=true
GMS_OPENAI_API_KEY=발급받은_GMS_키
GMS_OPENAI_MODEL=gpt-4o-mini
GMS_OPENAI_EMBEDDING_MODEL=text-embedding-3-small
```

끄고 실행하면 LLM 호출 없이 Tool/RAG fallback 결과만 반환한다.

```properties
NEOULTEO_AI_ENABLED=false
```

## 8. 앞으로 확장할 부분

우수상 목표로 확장한다면 다음 순서가 좋다.

1. 여행계획 평가를 `/app/ai/travel-assistant`와 같은 RAG+Tool 흐름으로 통합
2. Tool 호출 로그를 DB에 저장하거나 Langfuse로 모니터링
3. 여행 코스 복사/공유 데이터를 RAG 문서로 추가
4. 핫플레이스 후기 데이터를 RAG 문서로 추가
5. 날씨 API를 실제 연동해서 날짜별 여행 조언 제공
6. Vector Store를 붙여 관광지 5만 건 전체를 안정적으로 검색
7. LLM이 JSON plan을 생성하고 서버가 검증한 뒤 ToolExecutor가 실행하는 Planner 구조로 확장
