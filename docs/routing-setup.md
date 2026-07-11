# 실제 이동 경로 설정

여행 계획의 자동차·도보 경로는 TMAP OpenAPI를 백엔드에서 호출합니다. 브라우저에 키가 노출되지 않도록 서버 환경 변수로 설정합니다.

```powershell
$env:TMAP_APP_KEY="발급받은_TMAP_AppKey"
cd backend
.\mvnw.cmd spring-boot:run
```

또는 Git에 포함되지 않는 `backend/src/main/resources/application-local.properties`에 다음 값을 넣을 수 있습니다.

```properties
neoulteo.routing.tmap-app-key=발급받은_TMAP_AppKey
```

사용 API는 자동차 경로 `/tmap/routes`와 보행자 경로 `/tmap/routes/pedestrian`입니다. 키가 없으면 여행 계획 화면에 설정 안내 오류가 표시되며 직선 경로로 대체하지 않습니다.
