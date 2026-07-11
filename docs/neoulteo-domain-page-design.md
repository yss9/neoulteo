# Neoulteo 도메인 및 페이지 설계 문서

## 1. 문서 목적

이 문서는 Neoulteo 프로젝트를 기능 단위로 정리하기 위한 설계 문서이다.

Neoulteo는 공공 관광 데이터, 사용자 핫플레이스, 지도 기반 여행 계획, 커뮤니티, AI 추천 기능을 함께 제공하는 여행 플랫폼을 목표로 한다. 기능을 무작정 화면 단위로 나누기보다, 백엔드에서는 도메인 단위로 책임을 나누고, 프론트엔드에서는 사용자가 실제로 수행하는 흐름에 따라 페이지를 구성하는 방식이 적절하다.

이 문서에서는 다음 내용을 정리한다.

- 도메인의 의미
- 도메인별 주요 기능
- 도메인별 백엔드 패키지 구조
- 도메인별 필요한 기술
- 전체 웹 페이지 구성
- 페이지별 수행 기능
- Spring AI, Spring Batch, 지도 기능을 포함했을 때의 확장 방향

## 2. 도메인의 의미

도메인은 프로젝트에서 하나의 큰 기능 영역을 의미한다.

예를 들어 회원 기능, 관광지 검색 기능, 여행 계획 기능, 핫플레이스 기능은 서로 다른 관심사를 가진다. 이런 기능을 하나의 폴더에 섞어두면 프로젝트가 커질수록 관리가 어려워진다. 그래서 실무에서는 보통 기능의 책임에 따라 도메인을 나누고, 각 도메인 내부에서 Controller, DTO, Service, DAO 또는 Mapper, Model 등을 나누는 구조를 많이 사용한다.

Neoulteo에서 사용할 수 있는 대표 도메인은 다음과 같다.

```text
member
auth
attraction
plan
hotplace
community
ai
batch
file
admin
global
```

`global`은 특정 기능 도메인이 아니라 공통 설정, 보안, 예외 처리, 공통 응답 객체 등을 관리하는 영역이다.

## 3. 백엔드 기본 패키지 구조

Spring Boot 기준으로 도메인 단위 패키지 구조는 다음과 같이 가져갈 수 있다.

```text
backend/src/main/java/com/neoulteo/
  NeoulteoApplication.java

  domain/
    member/
      controller/
      dto/
      mapper/
      service/
      model/

    attraction/
      controller/
      dto/
      mapper/
      service/
      model/

    plan/
      controller/
      dto/
      mapper/
      service/
      model/

    hotplace/
      controller/
      dto/
      mapper/
      service/
      model/

    community/
      controller/
      dto/
      mapper/
      service/
      model/

    ai/
      controller/
      dto/
      service/
      tool/
      planner/
      rag/

    batch/
      job/
      step/
      tasklet/
      reader/
      processor/
      writer/

    file/
      controller/
      dto/
      service/
      storage/

    admin/
      controller/
      dto/
      service/

  global/
    config/
    security/
    exception/
    response/
    util/
```

각 폴더의 의미는 다음과 같다.

```text
controller
- HTTP 요청을 받는 계층
- Vue 프론트엔드에서 호출하는 REST API 엔드포인트를 정의한다.

dto
- 요청과 응답에 사용되는 데이터 객체
- DB 모델을 그대로 외부에 노출하지 않기 위해 사용한다.

service
- 핵심 비즈니스 로직을 처리한다.
- Controller는 최대한 얇게 두고, 실제 판단과 처리는 Service에서 한다.

mapper 또는 dao
- MyBatis를 사용해 DB에 접근하는 계층
- SQL Mapper XML 또는 Annotation 기반 SQL과 연결된다.

model 또는 entity
- DB 테이블 구조와 가까운 객체
- MyBatis에서는 Entity라는 이름보다 Model, Domain, VO 등의 이름도 자주 사용한다.

global
- 보안 설정, CORS 설정, 예외 처리, 공통 응답 포맷, 공통 유틸 등을 관리한다.
```

## 4. Member / Auth 도메인

### 4.1 도메인 목적

Member/Auth 도메인은 회원가입, 로그인, 로그아웃, 로그인 상태 유지, 회원 정보 수정, 회원 탈퇴를 담당한다.

Neoulteo는 여행 계획 저장, 핫플레이스 등록, 커뮤니티 글 작성처럼 로그인 사용자를 기준으로 동작하는 기능이 많다. 따라서 회원 인증 도메인은 다른 도메인의 기반이 된다.

### 4.2 주요 기능

```text
회원가입
로그인
로그아웃
Remember-me 로그인 상태 유지
현재 로그인 사용자 조회
내 정보 조회
이름 변경
비밀번호 변경
비밀번호 재설정
회원 탈퇴
인증 필요 API 보호
```

### 4.3 백엔드 구성 예시

```text
domain/member/
  controller/
    MemberController.java
    AuthController.java

  dto/
    SignupRequest.java
    LoginRequest.java
    MemberResponse.java
    UpdateProfileRequest.java
    UpdatePasswordRequest.java
    DeleteMemberRequest.java

  mapper/
    MemberMapper.java

  service/
    MemberService.java
    AuthService.java

  model/
    Member.java
```

### 4.4 필요한 기술

```text
Spring Security
- 로그인, 로그아웃, 인증 상태 확인을 처리한다.
- 보호된 API에 로그인 여부를 적용한다.

BCrypt
- 비밀번호를 평문으로 저장하지 않고 해시 처리한다.

Session
- 로그인 상태를 서버 세션으로 유지한다.
- JSESSIONID 쿠키를 사용한다.

Remember-me Cookie
- 브라우저를 닫았다가 다시 열어도 로그인 상태를 유지한다.
- remember-me 쿠키를 사용한다.

MyBatis
- 회원 데이터 조회, 저장, 수정, 삭제 SQL을 처리한다.

MySQL
- 회원 정보를 저장한다.

Axios credentials
- 프론트엔드에서 세션 쿠키와 remember-me 쿠키를 백엔드 요청에 포함하기 위해 필요하다.
```

### 4.5 관련 프론트엔드 기능

```text
로그인 폼
회원가입 폼
로그아웃 버튼
상단 헤더의 로그인 상태 표시
프로필 정보 표시
이름 변경 폼
비밀번호 변경 폼
회원 탈퇴 폼
로그인 유지 체크박스
```

## 5. Attraction 도메인

### 5.1 도메인 목적

Attraction 도메인은 공공 관광 데이터를 조회하고 지도에 표시하는 기능을 담당한다.

Neoulteo의 기본 데이터는 관광지, 숙박, 음식점, 문화시설, 공연, 여행코스, 쇼핑 등의 관광 정보이다. 사용자는 지역, 구/군, 카테고리, 키워드로 관광지를 검색하고, 결과를 지도 마커와 리스트로 확인할 수 있어야 한다.

### 5.2 주요 기능

```text
지역 목록 조회
구/군 목록 조회
카테고리 목록 조회
지역별 관광지 조회
구/군별 관광지 조회
카테고리별 관광지 조회
키워드 검색
관광지 상세 조회
지도 마커 표시용 좌표 제공
검색 결과 리스트 제공
관광지를 여행 계획에 추가
```

### 5.3 백엔드 구성 예시

```text
domain/attraction/
  controller/
    AttractionController.java

  dto/
    AttractionSearchRequest.java
    AttractionResponse.java
    AttractionDetailResponse.java
    AreaResponse.java
    DistrictResponse.java
    CategoryResponse.java

  mapper/
    AttractionMapper.java

  service/
    AttractionService.java

  model/
    Attraction.java
    Area.java
    District.java
    ContentType.java
```

### 5.4 필요한 기술

```text
MyBatis
- 관광지 검색 조건에 따른 SQL 처리를 담당한다.

MySQL
- 관광지, 지역, 구/군, 카테고리 정보를 저장한다.

Kakao Map JavaScript API
- 지도 표시, 마커 표시, 지도 이동, 확대/축소를 처리한다.

Tour API 데이터
- 관광지 기본 데이터의 출처가 된다.

Axios
- Vue 프론트엔드에서 Spring Boot API를 호출한다.

Pagination 또는 Limit
- 관광지 데이터가 많기 때문에 한 번에 너무 많은 데이터를 내려주지 않도록 한다.
```

### 5.5 관련 프론트엔드 기능

```text
지역 선택 드롭다운
구/군 선택 드롭다운
카테고리 선택 드롭다운
키워드 입력창
검색 버튼
지도 마커 표시
리스트 클릭 시 지도 포커스
선택된 관광지 강조
계획에 추가 버튼
```

## 6. Plan 도메인

### 6.1 도메인 목적

Plan 도메인은 사용자가 여행 일정을 만들고 저장하는 핵심 도메인이다.

단순히 장소 이름을 저장하는 수준이 아니라, 지도 위에서 선택한 장소들을 번호 마커와 경로 형태로 보여주고, 장소 순서를 바꾸고, 저장된 계획을 다시 불러올 수 있어야 한다.

Neoulteo에서 가장 서비스 완성도를 높일 수 있는 도메인이다.

### 6.2 주요 기능

```text
여행 계획 생성
내 여행 계획 목록 조회
여행 계획 상세 조회
여행 계획 수정
여행 계획 삭제
관광지 장소 추가
핫플레이스 장소 추가
장소 순서 변경
지도에 번호 마커 표시
장소 간 경로 표시
Day별 일정 관리
계획 공개/비공개 설정
공유 게시판으로 내보내기
AI 추천 결과를 계획으로 저장
```

### 6.3 백엔드 구성 예시

```text
domain/plan/
  controller/
    PlanController.java

  dto/
    CreatePlanRequest.java
    UpdatePlanRequest.java
    PlanResponse.java
    PlanDetailResponse.java
    PlanPlaceRequest.java
    PlanPlaceResponse.java
    ReorderPlanPlacesRequest.java

  mapper/
    PlanMapper.java

  service/
    PlanService.java

  model/
    Plan.java
    PlanPlace.java
```

### 6.4 필요한 기술

```text
Spring Security
- 로그인한 사용자만 자신의 계획을 생성, 수정, 삭제할 수 있도록 한다.

MyBatis
- 여행 계획, 계획 장소, 장소 순서를 DB에 저장한다.

MySQL
- plan, plan_place 같은 테이블을 사용한다.

Kakao Map API
- 계획에 포함된 장소를 지도 마커로 표시한다.

좌표 데이터
- 위도, 경도 기반으로 지도 위치와 경로를 표시한다.

Vue Drag and Drop
- 장소 순서를 사용자가 직접 변경할 수 있게 한다.

REST API
- 계획 생성, 수정, 삭제, 조회 요청을 처리한다.
```

### 6.5 관련 프론트엔드 기능

```text
계획 제목 입력
여행 날짜 입력
지역 선택
관광지 검색 후 계획에 추가
핫플레이스 검색 후 계획에 추가
지도 번호 마커 표시
장소 순서 변경
Day별 일정 구분
저장 버튼
공개 여부 선택
계획 상세 보기
```

### 6.6 추천 화면 구성

```text
좌측
- 내 계획 목록
- 새 계획 만들기

중앙
- 지도
- 번호 마커
- 경로 라인

우측
- 일정 편집 패널
- 장소 목록
- 장소 순서 변경
- 저장 버튼
```

## 7. Hotplace 도메인

### 7.1 도메인 목적

Hotplace 도메인은 사용자가 직접 발견한 장소를 등록하고 공유하는 기능을 담당한다.

Attraction 도메인이 공공 관광 데이터 기반이라면, Hotplace 도메인은 사용자 참여 기반이다. 관광지 API에 없는 장소, 사진 명소, 카페, 산책길, 지역 주민 추천 장소 등을 등록할 수 있어야 한다.

### 7.2 주요 기능

```text
핫플레이스 등록
핫플레이스 목록 조회
핫플레이스 상세 조회
핫플레이스 수정
핫플레이스 삭제
이미지 업로드
지역별 필터
태그별 필터
최신순 정렬
인기순 정렬
좋아요
조회수
댓글
내가 등록한 핫플레이스 조회
지도에서 위치 표시
여행 계획에 추가
AI 태그 자동 생성
```

### 7.3 백엔드 구성 예시

```text
domain/hotplace/
  controller/
    HotplaceController.java

  dto/
    CreateHotplaceRequest.java
    UpdateHotplaceRequest.java
    HotplaceResponse.java
    HotplaceDetailResponse.java
    HotplaceSearchRequest.java
    HotplaceLikeResponse.java

  mapper/
    HotplaceMapper.java
    HotplaceLikeMapper.java

  service/
    HotplaceService.java
    HotplaceLikeService.java

  model/
    Hotplace.java
    HotplaceImage.java
    HotplaceLike.java
    HotplaceTag.java
```

### 7.4 필요한 기술

```text
MultipartFile
- 사용자가 업로드하는 이미지를 처리한다.

Local Storage 또는 Cloud Storage
- 이미지 파일을 저장한다.

Static Resource Mapping
- 저장한 이미지를 브라우저에서 접근할 수 있게 한다.

MyBatis
- 핫플레이스, 이미지, 태그, 좋아요 데이터를 처리한다.

MySQL
- 핫플레이스 관련 테이블을 저장한다.

Kakao Map API
- 핫플레이스 위치를 지도에 표시한다.

Spring Security
- 작성자만 수정/삭제할 수 있도록 한다.

Spring AI
- 핫플레이스 설명을 기반으로 태그를 자동 생성할 수 있다.
```

### 7.5 관련 프론트엔드 기능

```text
핫플레이스 카드 목록
지역 필터
태그 필터
최신순/인기순 정렬
등록 버튼
이미지 업로드 폼
지도 위치 선택
좋아요 버튼
댓글 영역
계획에 추가 버튼
```

## 8. Community 도메인

### 8.1 도메인 목적

Community 도메인은 공지사항과 사용자 공유 게시판을 담당한다.

여행 계획을 다른 사용자와 공유하거나, 공지사항을 확인하거나, 후기와 질문을 남기는 기능을 제공한다.

### 8.2 주요 기능

```text
공지사항 목록 조회
공지사항 상세 조회
관리자 공지사항 작성
관리자 공지사항 수정
관리자 공지사항 삭제
공유 게시글 작성
공유 게시글 목록 조회
공유 게시글 상세 조회
공유 게시글 수정
공유 게시글 삭제
댓글 작성
댓글 삭제
좋아요
조회수
게시글 검색
여행 계획 공유
공유 계획 내 계획으로 복사
```

### 8.3 백엔드 구성 예시

```text
domain/community/
  controller/
    NoticeController.java
    BoardController.java
    CommentController.java

  dto/
    NoticeRequest.java
    NoticeResponse.java
    BoardRequest.java
    BoardResponse.java
    BoardDetailResponse.java
    CommentRequest.java
    CommentResponse.java

  mapper/
    NoticeMapper.java
    BoardMapper.java
    CommentMapper.java

  service/
    NoticeService.java
    BoardService.java
    CommentService.java

  model/
    Notice.java
    BoardPost.java
    Comment.java
```

### 8.4 필요한 기술

```text
MyBatis
- 게시글, 댓글, 공지사항 CRUD를 처리한다.

MySQL
- 게시글, 댓글, 좋아요, 조회수 데이터를 저장한다.

Spring Security
- 작성자 본인만 수정/삭제할 수 있게 한다.
- 공지사항 작성은 관리자 권한만 허용한다.

Pagination
- 게시글 목록을 페이지 단위로 조회한다.

검색 조건 처리
- 제목, 내용, 작성자, 지역, 태그 등으로 검색한다.
```

### 8.5 관련 프론트엔드 기능

```text
공지사항 탭
여행 계획 공유 탭
후기/질문 탭
게시글 목록
게시글 상세
게시글 작성 폼
댓글 작성
좋아요
검색
페이지네이션
```

## 9. AI 도메인

### 9.1 도메인 목적

AI 도메인은 Spring AI를 활용해 여행 추천, 코스 생성, 자연어 상담, RAG 기반 답변을 제공한다.

중요한 점은 단순히 챗봇을 붙이는 것이 아니다. AI가 Neoulteo의 실제 API와 DB 데이터를 활용해서 사용자에게 여행 계획을 제안하고, 그 결과를 실제 여행 계획으로 저장할 수 있어야 한다.

### 9.2 주요 기능

```text
AI 여행 코스 추천
자연어 요청 분석
관광지 검색 Tool 호출
핫플레이스 검색 Tool 호출
날씨 조회 Tool 호출
사용자 취향 조회 Tool 호출
여행 계획 저장 Tool 호출
추천 결과 지도 표시
RAG 기반 여행 질문 답변
AI 코스 이름 생성
AI 코스 설명 생성
핫플레이스 태그 자동 생성
공유 게시글 요약
```

### 9.3 백엔드 구성 예시

```text
domain/ai/
  controller/
    AiController.java
    AiPlannerController.java

  dto/
    AiChatRequest.java
    AiChatResponse.java
    AiPlanRequest.java
    AiPlanResponse.java
    AiPlanPlaceResponse.java

  service/
    AiChatService.java
    AiPlannerService.java
    RagService.java

  tool/
    AttractionTool.java
    HotplaceTool.java
    PlanTool.java
    WeatherTool.java
    UserTool.java

  planner/
    AiPlanner.java
    AiPlannerAssistant.java
    Plan.java
    PlanStep.java
    ToolExecutor.java

  rag/
    DocumentIngestionService.java
    EmbeddingService.java
    VectorSearchService.java
```

### 9.4 필요한 기술

```text
Spring AI
- Java/Spring 기반으로 LLM을 사용하기 위한 핵심 기술이다.

ChatClient
- LLM에게 프롬프트를 보내고 응답을 받는다.

Tool Calling
- LLM이 필요한 기능을 판단해 Spring Bean 형태의 도구를 호출하게 한다.

RAG
- DB나 문서에서 관련 정보를 검색해 LLM 응답에 근거로 제공한다.

Vector Store
- 관광지 설명, 핫플레이스 설명, 공유 게시글 등을 벡터로 저장하고 검색한다.

Embedding Model
- 문장을 벡터로 변환한다.

Prompt Template
- 일정한 형식으로 프롬프트를 구성한다.

Langfuse
- LLM 호출, 도구 호출, 응답 시간, 비용, 토큰 사용량을 모니터링한다.

OpenAI / Gemini / Claude
- LLM 제공자로 사용할 수 있다.
```

### 9.5 Tool Calling 흐름

```text
1. 사용자가 질문한다.
2. AgentController가 요청을 받는다.
3. ChatClient가 LLM에게 질문과 사용할 수 있는 Tool 목록을 전달한다.
4. LLM이 질문을 분석하고 필요한 Tool 호출을 결정한다.
5. Spring AI가 Tool을 실제 Java Service와 연결해 실행한다.
6. Tool 실행 결과가 LLM에게 다시 전달된다.
7. LLM은 Tool 결과를 바탕으로 최종 답변을 생성한다.
8. 프론트엔드는 최종 답변과 지도 표시용 데이터를 보여준다.
```

### 9.6 Planner 기반 흐름

```text
1. 사용자가 자연어로 여행 계획을 요청한다.
2. AiPlanner가 질문을 분석해 실행 계획을 만든다.
3. 실행 계획은 여러 개의 PlanStep으로 구성된다.
4. AiPlannerAssistant가 LLM 응답을 파싱하고 검증한다.
5. ToolExecutor가 PlanStep 순서대로 Tool을 실행한다.
6. 각 Tool 실행 결과를 모은다.
7. LLM에게 실행 결과를 전달해 최종 여행 계획 설명을 생성한다.
8. 사용자가 원하면 Plan 도메인에 저장한다.
```

### 9.7 Neoulteo에 적용 가능한 AI 기능 예시

```text
"광주 동구에서 문화시설 위주로 5시간 코스 짜줘"
- AttractionTool로 광주 동구 문화시설 검색
- HotplaceTool로 주변 인기 장소 검색
- WeatherTool로 날씨 확인
- PlanTool로 지도 경로 형태의 계획 생성
- LLM이 추천 이유와 일정 설명 생성

"비 오는 날 대구에서 갈 만한 곳 추천해줘"
- WeatherTool로 날씨 확인
- AttractionTool로 실내 관광지 검색
- RAG로 관련 후기 검색
- LLM이 추천 목록 생성

"내가 저장한 장소들로 이동 동선 괜찮은지 봐줘"
- PlanTool로 기존 계획 조회
- 좌표를 기반으로 순서 분석
- LLM이 개선 방향 설명
```

## 10. Batch 도메인

### 10.1 도메인 목적

Batch 도메인은 사용자가 직접 요청하지 않아도 주기적으로 실행되는 작업을 담당한다.

관광지 데이터 갱신, 인기 핫플레이스 집계, AI 태그 생성, 오늘의 추천 코스 생성처럼 시간이 오래 걸리거나 반복적으로 필요한 작업에 적합하다.

### 10.2 주요 기능

```text
관광지 데이터 갱신
지역 데이터 갱신
카테고리 데이터 갱신
AI 태그 자동 생성
오늘의 추천 코스 생성
인기 핫플레이스 집계
지역별 인기 관광지 집계
누락 이미지 점검
누락 좌표 점검
RAG용 문서 생성
벡터 데이터 갱신
월간 여행 리포트 생성
```

### 10.3 백엔드 구성 예시

```text
domain/batch/
  job/
    AttractionSyncJobConfig.java
    AiTagGenerateJobConfig.java
    DailyCourseGenerateJobConfig.java
    VectorRefreshJobConfig.java

  step/
    AttractionSyncStepConfig.java
    HotplaceTagStepConfig.java
    DailyCourseStepConfig.java

  tasklet/
    AttractionSyncTasklet.java
    DailyCourseTasklet.java
    VectorRefreshTasklet.java

  reader/
    AttractionReader.java
    HotplaceReader.java

  processor/
    AttractionProcessor.java
    HotplaceTagProcessor.java

  writer/
    AttractionWriter.java
    AiTagWriter.java
    VectorDocumentWriter.java
```

### 10.4 필요한 기술

```text
Spring Batch
- 대량 데이터 처리와 정기 작업을 구조적으로 관리한다.

Spring Scheduler
- 정해진 시간에 Batch Job을 실행한다.

MyBatis / MySQL
- Batch 결과를 DB에 저장한다.

Tour API
- 관광지 데이터를 주기적으로 갱신한다.

Spring AI
- 태그 생성, 추천 코스 생성, 요약 등에 사용한다.

Vector Store
- RAG 검색용 벡터 데이터를 갱신한다.
```

### 10.5 사용자 화면과의 연결

```text
오늘의 추천 코스
- Batch가 매일 생성한 추천 코스를 메인 페이지에 표시한다.

인기 핫플레이스
- Batch가 조회수, 좋아요, 댓글 수를 기준으로 집계한 결과를 표시한다.

AI 태그
- Batch가 생성한 태그를 핫플레이스 카드와 검색 필터에 사용한다.

지역별 추천
- Batch가 지역별 인기 관광지와 핫플레이스를 계산해 표시한다.
```

## 11. File / Image 도메인

### 11.1 도메인 목적

File 도메인은 이미지 업로드와 파일 저장을 담당한다.

핫플레이스, 커뮤니티 게시글, 프로필 이미지 등에서 공통으로 사용할 수 있다.

### 11.2 주요 기능

```text
이미지 업로드
이미지 URL 반환
이미지 삭제
파일 크기 제한
파일 확장자 검증
저장 경로 관리
기본 이미지 처리
```

### 11.3 백엔드 구성 예시

```text
domain/file/
  controller/
    FileController.java

  dto/
    FileUploadResponse.java

  service/
    FileService.java

  storage/
    LocalFileStorage.java
```

### 11.4 필요한 기술

```text
MultipartFile
- Spring에서 파일 업로드를 처리한다.

Local File Storage
- 프로젝트의 storage 폴더 또는 지정 경로에 이미지를 저장한다.

Static Resource Mapping
- 업로드한 이미지를 브라우저에서 접근 가능하게 한다.

UUID 파일명
- 원본 파일명 충돌을 방지한다.

확장자 검증
- 이미지 파일만 업로드되도록 제한한다.
```

## 12. Admin 도메인

### 12.1 도메인 목적

Admin 도메인은 관리자 기능을 담당한다.

프로젝트 규모가 작다면 처음부터 크게 만들 필요는 없지만, 공지사항 작성, Batch 실행 상태 확인, 신고 관리 정도가 있으면 서비스 완성도가 좋아진다.

### 12.2 주요 기능

```text
공지사항 작성
공지사항 수정
공지사항 삭제
회원 목록 조회
부적절한 게시글 관리
Batch 실행 결과 조회
관광지 데이터 갱신 상태 확인
AI 호출 로그 확인
```

### 12.3 필요한 기술

```text
Spring Security Role
- ADMIN 권한을 가진 사용자만 접근하게 한다.

MyBatis
- 관리자 조회 기능과 상태 조회 기능을 처리한다.

Spring Batch Metadata
- Batch 실행 이력을 확인한다.

Langfuse
- AI 호출 로그와 Tool 호출 흐름을 확인한다.
```

## 13. 전체 프론트엔드 구조

Vue 프로젝트 기준으로 프론트엔드는 다음과 같이 구성할 수 있다.

```text
frontend/src/
  app/
    router/
      index.js
    stores/
      authStore.js

  components/
    common/
      AppHeader.vue
      AppFooter.vue
      BaseButton.vue
      BaseInput.vue
      BaseModal.vue
      LoadingSpinner.vue

    map/
      KakaoMap.vue
      MapMarker.vue
      RouteMap.vue

    attraction/
      AttractionFilter.vue
      AttractionList.vue
      AttractionCard.vue

    plan/
      PlanEditor.vue
      PlanPlaceList.vue
      PlanRouteMap.vue

    hotplace/
      HotplaceCard.vue
      HotplaceForm.vue

    community/
      PostList.vue
      CommentList.vue

  pages/
    HomePage.vue
    LoginPage.vue
    SignupPage.vue
    ProfilePage.vue
    AttractionSearchPage.vue
    AttractionDetailPage.vue
    PlanListPage.vue
    PlanCreatePage.vue
    PlanDetailPage.vue
    HotplaceListPage.vue
    HotplaceCreatePage.vue
    HotplaceDetailPage.vue
    CommunityPage.vue
    NoticeDetailPage.vue
    BoardPostDetailPage.vue
    AiPlannerPage.vue
    AdminPage.vue

  api/
    authApi.js
    memberApi.js
    attractionApi.js
    planApi.js
    hotplaceApi.js
    communityApi.js
    aiApi.js
    fileApi.js

  styles/
    main.css
    layout.css
    components.css
```

## 14. 전체 페이지 구성

Neoulteo가 모든 기능을 구현했을 때 필요한 페이지는 다음과 같다.

```text
메인 페이지
로그인 페이지
회원가입 페이지
프로필 페이지
관광지 검색 페이지
관광지 상세 페이지
여행 계획 목록 페이지
여행 계획 작성 페이지
여행 계획 상세 페이지
여행 계획 수정 페이지
핫플레이스 목록 페이지
핫플레이스 등록 페이지
핫플레이스 상세 페이지
핫플레이스 수정 페이지
커뮤니티 페이지
공지사항 상세 페이지
공유 게시글 작성 페이지
공유 게시글 상세 페이지
AI 여행 플래너 페이지
AI 추천 결과 페이지
관리자 페이지
```

## 15. 페이지별 기능 설명

### 15.1 메인 페이지

메인 페이지는 사용자가 서비스의 핵심 기능으로 빠르게 진입할 수 있도록 구성한다.

수행 기능:

```text
서비스 대표 배너 표시
빠른 관광지 검색
오늘의 추천 코스 표시
인기 핫플레이스 표시
최근 공유 여행 계획 표시
AI 여행 플래너 바로가기
로그인 상태에 따른 버튼 표시
```

추천 구성:

```text
상단 Header
Hero Banner
빠른 검색 영역
오늘의 추천 코스
인기 핫플레이스
최근 공유 여행 계획
Footer
```

### 15.2 로그인 페이지

로그인 페이지는 사용자가 이메일과 비밀번호로 로그인하는 페이지이다.

수행 기능:

```text
이메일 입력
비밀번호 입력
Remember-me 체크
로그인 요청
로그인 실패 메시지 표시
회원가입 페이지 이동
로그인 성공 후 이전 페이지 또는 메인 페이지 이동
```

필요한 API:

```text
POST /api/auth/login
GET /api/auth/me
```

### 15.3 회원가입 페이지

회원가입 페이지는 신규 사용자가 계정을 만드는 페이지이다.

수행 기능:

```text
이름 입력
이메일 입력
비밀번호 입력
회원가입 요청
가입 성공 메시지 표시
가입 후 로그인 페이지 또는 메인 페이지 이동
```

필요한 API:

```text
POST /api/members
```

### 15.4 프로필 페이지

프로필 페이지는 로그인한 사용자가 자신의 정보를 관리하는 페이지이다.

수행 기능:

```text
내 정보 조회
이름 변경
비밀번호 변경
회원 탈퇴
내 여행 계획 목록 바로가기
내 핫플레이스 목록 바로가기
로그아웃
```

필요한 API:

```text
GET /api/members/me
PATCH /api/members/me
PATCH /api/members/me/password
DELETE /api/members/me
```

### 15.5 관광지 검색 페이지

관광지 검색 페이지는 Neoulteo의 주요 탐색 화면이다.

수행 기능:

```text
지역 선택
구/군 선택
카테고리 선택
키워드 검색
관광지 목록 조회
관광지 지도 마커 표시
리스트 클릭 시 지도 포커스
관광지 상세 보기
여행 계획에 추가
```

필요한 API:

```text
GET /api/areas
GET /api/areas/{areaCode}/districts
GET /api/content-types
GET /api/attractions
GET /api/attractions/{id}
```

추천 레이아웃:

```text
좌측 필터 패널
중앙 지도
우측 관광지 리스트
```

### 15.6 관광지 상세 페이지

관광지 상세 페이지는 특정 관광지의 상세 정보를 보여준다.

수행 기능:

```text
관광지 이름 표시
관광지 이미지 표시
주소 표시
카테고리 표시
지도 위치 표시
상세 설명 표시
여행 계획에 추가
관련 핫플레이스 표시
```

필요한 API:

```text
GET /api/attractions/{id}
GET /api/hotplaces?nearAttractionId={id}
```

### 15.7 여행 계획 목록 페이지

여행 계획 목록 페이지는 사용자가 저장한 여행 계획을 조회하는 페이지이다.

수행 기능:

```text
내 여행 계획 목록 조회
계획 제목 표시
여행 지역 표시
여행 날짜 표시
공개/비공개 표시
새 계획 만들기
계획 상세 이동
계획 삭제
```

필요한 API:

```text
GET /api/plans/me
DELETE /api/plans/{planId}
```

### 15.8 여행 계획 작성 페이지

여행 계획 작성 페이지는 사용자가 지도 기반으로 여행 계획을 만드는 페이지이다.

수행 기능:

```text
계획 제목 입력
여행 날짜 입력
지역 선택
관광지 검색
핫플레이스 검색
장소 추가
장소 삭제
장소 순서 변경
지도 번호 마커 표시
경로 표시
AI 추천 요청
계획 저장
```

필요한 API:

```text
POST /api/plans
GET /api/attractions
GET /api/hotplaces
POST /api/ai/plans/recommend
```

추천 레이아웃:

```text
좌측 검색 영역
중앙 지도
우측 일정 편집 영역
```

### 15.9 여행 계획 상세 페이지

여행 계획 상세 페이지는 저장된 여행 계획을 보여준다.

수행 기능:

```text
계획 제목 조회
여행 날짜 조회
장소 목록 조회
Day별 일정 조회
지도 번호 마커 표시
경로 표시
공유하기
수정하기
삭제하기
```

필요한 API:

```text
GET /api/plans/{planId}
PATCH /api/plans/{planId}/visibility
DELETE /api/plans/{planId}
```

### 15.10 여행 계획 수정 페이지

여행 계획 수정 페이지는 기존 계획의 장소와 순서를 수정하는 페이지이다.

수행 기능:

```text
계획 제목 수정
여행 날짜 수정
장소 추가
장소 삭제
장소 순서 변경
지도 경로 다시 계산
저장
```

필요한 API:

```text
GET /api/plans/{planId}
PUT /api/plans/{planId}
PUT /api/plans/{planId}/places/order
```

### 15.11 핫플레이스 목록 페이지

핫플레이스 목록 페이지는 사용자들이 등록한 장소를 탐색하는 페이지이다.

수행 기능:

```text
핫플레이스 목록 조회
지역 필터
태그 필터
최신순 정렬
인기순 정렬
카드 형태 표시
좋아요
지도 보기
등록 페이지 이동
```

필요한 API:

```text
GET /api/hotplaces
POST /api/hotplaces/{id}/like
DELETE /api/hotplaces/{id}/like
```

### 15.12 핫플레이스 등록 페이지

핫플레이스 등록 페이지는 사용자가 직접 장소를 등록하는 페이지이다.

수행 기능:

```text
장소명 입력
주소 입력
지도에서 위치 선택
위도/경도 저장
이미지 업로드
설명 입력
태그 입력
등록
```

필요한 API:

```text
POST /api/files/images
POST /api/hotplaces
```

### 15.13 핫플레이스 상세 페이지

핫플레이스 상세 페이지는 특정 핫플레이스의 상세 정보를 보여준다.

수행 기능:

```text
이미지 조회
장소명 조회
작성자 조회
설명 조회
태그 조회
지도 위치 표시
좋아요
댓글 조회
댓글 작성
계획에 추가
수정/삭제
```

필요한 API:

```text
GET /api/hotplaces/{id}
POST /api/hotplaces/{id}/like
POST /api/hotplaces/{id}/comments
DELETE /api/hotplaces/{id}
```

### 15.14 커뮤니티 페이지

커뮤니티 페이지는 공지사항, 공유 게시판, 후기/질문 게시판을 탭 형태로 보여줄 수 있다.

수행 기능:

```text
공지사항 목록 조회
공유 게시글 목록 조회
후기/질문 목록 조회
게시글 검색
게시글 작성 페이지 이동
페이지네이션
```

필요한 API:

```text
GET /api/notices
GET /api/boards
```

### 15.15 공지사항 상세 페이지

공지사항 상세 페이지는 관리자 공지를 보여준다.

수행 기능:

```text
공지 제목 조회
공지 내용 조회
작성일 조회
목록으로 이동
관리자일 경우 수정/삭제
```

필요한 API:

```text
GET /api/notices/{noticeId}
PUT /api/notices/{noticeId}
DELETE /api/notices/{noticeId}
```

### 15.16 공유 게시글 작성 페이지

공유 게시글 작성 페이지는 사용자가 여행 계획이나 후기를 공유하는 페이지이다.

수행 기능:

```text
제목 입력
내용 입력
이미지 업로드
내 여행 계획 선택
태그 입력
게시글 작성
```

필요한 API:

```text
GET /api/plans/me
POST /api/files/images
POST /api/boards
```

### 15.17 공유 게시글 상세 페이지

공유 게시글 상세 페이지는 공유된 여행 계획이나 후기를 보여준다.

수행 기능:

```text
게시글 제목 조회
게시글 내용 조회
작성자 조회
연결된 여행 계획 지도 표시
댓글 조회
댓글 작성
좋아요
내 계획으로 복사
수정/삭제
```

필요한 API:

```text
GET /api/boards/{postId}
POST /api/boards/{postId}/comments
POST /api/boards/{postId}/like
POST /api/plans/copy-from-board/{postId}
```

### 15.18 AI 여행 플래너 페이지

AI 여행 플래너 페이지는 사용자가 자연어로 여행 계획을 요청하는 페이지이다.

수행 기능:

```text
자연어 여행 요청 입력
지역 선택
여행 기간 선택
동행 유형 선택
취향 선택
이동수단 선택
AI 추천 요청
추천 일정 표시
지도 경로 표시
추천 이유 표시
계획으로 저장
```

필요한 API:

```text
POST /api/ai/plans/recommend
POST /api/plans
```

추천 레이아웃:

```text
좌측 요청 입력 패널
중앙 추천 지도
우측 추천 일정표
```

### 15.19 AI 추천 결과 페이지

AI 추천 결과 페이지는 AI가 생성한 결과를 상세하게 보여준다.

수행 기능:

```text
AI 추천 코스 이름 표시
Day별 일정 표시
장소별 추천 이유 표시
지도 번호 마커 표시
경로 표시
일정 수정
내 계획으로 저장
공유하기
```

필요한 API:

```text
POST /api/ai/plans/recommend
POST /api/plans
```

### 15.20 관리자 페이지

관리자 페이지는 운영자가 서비스 데이터를 관리하는 페이지이다.

수행 기능:

```text
공지사항 작성
공지사항 수정
공지사항 삭제
Batch 실행 상태 조회
관광지 데이터 갱신 상태 조회
신고 게시글 관리
회원 목록 조회
AI 호출 로그 확인
```

필요한 API:

```text
GET /api/admin/batch/jobs
POST /api/admin/batch/jobs/{jobName}/run
GET /api/admin/members
GET /api/admin/reports
```

## 16. 전체 네비게이션 구성

상단 Header는 다음과 같이 구성할 수 있다.

```text
Neoulteo 로고
Attractions
Plans
Hotplaces
Community
AI Planner
Profile
Login / Logout
```

로그인 상태에 따라 다르게 보이는 요소:

```text
비로그인
- Login
- Signup

로그인
- 사용자 이름
- Profile
- Logout
```

## 17. 전체 디자인 컨셉

Neoulteo의 디자인은 단순 감성 여행 블로그보다 지도 기반 여행 플랫폼에 가까워야 한다.

추천 컨셉:

```text
지도 중심
공공 관광 데이터의 신뢰감
사용자 참여형 핫플레이스
AI 여행 플래너
깔끔한 여행 대시보드
```

추천 색상 방향:

```text
Primary
- Deep Teal
- Blue

Accent
- Fresh Green
- Warm Yellow

Background
- Light gray-blue
- White

Text
- Dark Navy
```

피해야 할 방향:

```text
너무 감성 블로그 같은 화면
너무 어두운 AI/SF 느낌
보라색 그라데이션 중심 디자인
카드만 지나치게 많은 랜딩페이지 구조
실제 기능보다 홍보 문구가 많은 화면
```

## 18. 이미지와 배너 필요 목록

프론트엔드를 보기 좋게 만들기 위해 필요한 이미지 종류는 다음과 같다.

```text
main-hero.png
- 메인 페이지 상단 배너
- 지도, 한국 여행, 경로, 관광지 분위기

attractions-hero.png
- 관광지 검색 페이지 배너
- 지도 마커와 여행지 탐색 분위기

plans-hero.png
- 여행 계획 페이지 배너
- 번호 마커, 경로, 일정표 느낌

hotplaces-hero.png
- 핫플레이스 페이지 배너
- 사용자 사진 카드, 지도, 위치 핀 느낌

ai-planner-hero.png
- AI 여행 플래너 페이지 배너
- AI 추천, 지도, 경로, 데이터 분석 느낌

empty-plan.png
- 아직 여행 계획이 없을 때 표시할 이미지

empty-hotplace.png
- 핫플레이스 목록이 비어 있을 때 표시할 이미지

default-place.png
- 관광지 이미지가 없을 때 사용하는 기본 이미지
```

이미지를 생성할 때는 다음 기준을 지키는 것이 좋다.

```text
이미지 안에 글자 넣지 않기
로고 넣지 않기
워터마크 넣지 않기
텍스트가 올라갈 여백 남기기
너무 어둡게 만들지 않기
너무 복잡한 중앙 구도 피하기
```

## 19. 우선순위 추천

모든 기능을 한 번에 구현하기보다 단계적으로 구현하는 것이 좋다.

### 19.1 1차 구현

```text
회원가입
로그인
로그아웃
Remember-me
관광지 검색
지도 마커 표시
핫플레이스 목록
핫플레이스 등록
간단한 여행 계획 저장
```

### 19.2 2차 구현

```text
지도 기반 여행 계획
번호 마커
장소 순서 변경
경로 저장
계획 상세 조회
계획 공유
커뮤니티 게시판
댓글
좋아요
```

### 19.3 3차 구현

```text
Spring AI 여행 플래너
Tool Calling
RAG 여행 상담
Spring Batch 추천 코스 생성
AI 태그 자동 생성
Langfuse 모니터링
```

## 20. 우수상 목표 기능 조합

관통 프로젝트에서 차별점을 만들기 위해서는 다음 조합이 가장 좋다.

```text
공공 관광 데이터
+ 사용자 핫플레이스
+ 지도 기반 여행 계획
+ 여행 계획 공유
+ Spring AI Tool Calling
+ RAG 기반 여행 상담
+ Spring Batch 자동 추천
```

서비스 설명은 다음과 같이 가져갈 수 있다.

```text
Neoulteo는 공공 관광 데이터와 사용자가 등록한 핫플레이스를 함께 활용해,
사용자가 지도 위에서 여행 코스를 만들고 공유할 수 있는 여행 계획 플랫폼이다.
Spring AI를 활용해 사용자의 자연어 요청을 분석하고,
관광지 검색, 핫플레이스 검색, 날씨 조회, 계획 저장 같은 서비스 기능을 Tool로 호출해
실제 데이터 기반 여행 코스를 추천한다.
```

## 21. 최종 도메인 요약

```text
member/auth
- 회원가입, 로그인, 로그아웃, 프로필, 탈퇴

attraction
- 공공 관광지 검색, 지도 마커, 상세 조회

plan
- 여행 계획 생성, 지도 경로 저장, 일정 관리

hotplace
- 사용자 등록 장소, 이미지, 태그, 좋아요

community
- 공지사항, 공유 게시판, 댓글, 좋아요

ai
- Spring AI, Tool Calling, RAG, AI 여행 플래너

batch
- 데이터 갱신, 추천 코스 생성, AI 태그 생성

file
- 이미지 업로드, 파일 저장, 이미지 URL 관리

admin
- 관리자 공지, 배치 상태, 신고 관리

global
- 보안, 예외 처리, 공통 응답, 설정
```

## 22. 최종 페이지 요약

```text
메인 페이지
- 빠른 검색, 추천 코스, 인기 핫플레이스, AI 플래너 진입

로그인 페이지
- 로그인, Remember-me, 로그인 실패 표시

회원가입 페이지
- 신규 회원 가입

프로필 페이지
- 내 정보 조회, 이름 변경, 비밀번호 변경, 탈퇴

관광지 검색 페이지
- 필터 검색, 지도 마커, 리스트, 계획 추가

관광지 상세 페이지
- 관광지 상세 정보, 지도 위치, 계획 추가

여행 계획 목록 페이지
- 내 계획 목록, 새 계획 생성, 삭제

여행 계획 작성 페이지
- 장소 추가, 지도 경로, 번호 마커, AI 추천, 저장

여행 계획 상세 페이지
- 저장된 계획 조회, 지도 경로, 공유, 수정, 삭제

핫플레이스 목록 페이지
- 사용자 등록 장소 조회, 필터, 좋아요

핫플레이스 등록 페이지
- 장소 등록, 이미지 업로드, 지도 위치 선택

핫플레이스 상세 페이지
- 상세 조회, 좋아요, 댓글, 계획 추가

커뮤니티 페이지
- 공지사항, 공유 게시글, 후기/질문

공지사항 상세 페이지
- 공지 상세 조회

공유 게시글 작성 페이지
- 여행 계획 공유 글 작성

공유 게시글 상세 페이지
- 공유 계획 조회, 댓글, 좋아요, 내 계획으로 복사

AI 여행 플래너 페이지
- 자연어 요청, AI 코스 생성, 지도 표시, 계획 저장

AI 추천 결과 페이지
- AI 결과 상세 확인, 일정 수정, 저장

관리자 페이지
- 공지 관리, 배치 상태, 신고 관리
```

