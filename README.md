# Stock Auto Trade - 백엔드 상세 문서

## 📦 프로젝트 구조

```
stock-auto-trade-Backend/
├── src/main/java/com/tr/autos/
│   ├── AutosApplication.java          # 메인 애플리케이션
│   ├── auth/                          # 인증 관련
│   │   ├── controller/
│   │   │   ├── AuthController.java    # 회원가입, 로그인, 로그아웃, 토큰 갱신
│   │   │   └── AdminController.java   # 관리자 헬스체크
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   │   ├── LoginRequestDto.java
│   │   │   │   └── SignupRequestDto.java
│   │   │   └── response/
│   │   │       ├── LoginResponseDto.java
│   │   │       └── SignupResponseDto.java
│   │   └── service/
│   │       └── AuthService.java       # 인증 비즈니스 로직
│   ├── config/                        # 설정 클래스
│   │   ├── SecurityConfig.java        # Spring Security 설정
│   │   ├── PasswordConfig.java        # BCrypt 인코더
│   │   ├── RedisConfig.java           # Redis 설정
│   │   ├── RestConfig.java            # RestTemplate 설정 (추후 RestClient로 변경)
│   │   └── AdminUserConfig.java       # 관리자 초기화
│   ├── domain/                        # 도메인 엔티티
│   │   ├── user/
│   │   │   ├── User.java              # 사용자 엔티티
│   │   │   ├── Role.java              # 역할 Enum (USER, ADMIN)
│   │   │   └── repository/UserRepository.java
│   │   ├── symbol/
│   │   │   ├── Symbol.java            # 종목 엔티티
│   │   │   └── repository/SymbolRepository.java
│   │   ├── watchlist/
│   │   │   └── Watchlist.java         # 관심종목 엔티티
│   │   └── quote/
│   │       ├── QuoteCache.java        # 시세 캐시 엔티티
│   │       ├── dto/                   # Quote 관련 DTO
│   │       └── repository/QuoteCacheJpaRepository.java
│   ├── jwt/                           # JWT 처리
│   │   ├── JwtTokenProvider.java      # 토큰 생성/검증
│   │   ├── JwtAuthenticationFilter.java # 인증 필터
│   │   └── JwtAuthEntryPoint.java     # 인증 실패 핸들러
│   ├── market/                        # 시장 데이터
│   │   ├── kis/                       # 한국투자증권 API 클라이언트
│   │   │   ├── controller/
│   │   │   │   ├── KisTokenController.java
│   │   │   │   └── DomesticMarketRankingController.java
│   │   │   ├── service/
│   │   │   │   ├── KisTokenService.java # KIS 토큰 관리
│   │   │   │   └── DomesticMarketRankingService.java
│   │   │   ├── KisAuthClient.java     # 인증 API 클라이언트
│   │   │   ├── KisMarketDataClient.java # 시세 API 클라이언트
│   │   │   └── dto/                   # KIS API DTO
│   │   └── dto/
│   ├── quote/                         # 시세 처리
│   │   ├── controller/
│   │   │   └── StockDetailController.java
│   │   ├── service/
│   │   │   ├── KisIntegrationService.java # KIS API 통합 서비스
│   │   │   ├── StockDetailReadService.java # 종목 상세 조회
│   │   │   ├── StockChartService.java # 차트 데이터 조회
│   │   │   ├── OverseasStockDetailService.java # 미국 종목 처리
│   │   │   ├── OverseasStockChartService.java
│   │   │   └── QuoteDailyUpdateService.java 
│   │   ├── admin/
│   │   │   └── AdminQuoteRefreshController.java # 수동 갱신
│   │   └── client/
│   │       └── KisQuoteClient.java    # 시세 API 클라이언트
│   ├── symbol/                        # 종목 검색
│   │   ├── controller/
│   │   │   └── SymbolController.java
│   │   ├── service/
│   │   │   └── SymbolService.java
│   │   └── dto/
│   ├── watchlist/                     # 관심종목
│   │   ├── controller/
│   │   │   └── WatchlistController.java
│   │   ├── service/
│   │   │   └── WatchlistService.java
│   │   ├── dto/
│   │   └── repository/WatchlistRepository.java
│   ├── error/                         # 에러 처리
│   └── utils/                         # 유틸리티
│       └── DataConversionUtils.java   # 데이터 변환 유틸
├── src/main/resources/
│   ├── application.yml                # 기본 설정
│   ├── application-local.yml          # 로컬 설정
│   └── db/migration/                  # Flyway 마이그레이션
│       ├── V1__init.sql               # 초기 스키마
│       ├── V2__quote__cache.sql       # 시세 캐시 테이블
│       ├── V3__add_index_on_symbols_ticker.sql
│       ├── V3_2__add_index_on_quote_cache_updated_at.sql
│       ├── V4__add_additional_symbols.sql
│       ├── V5__update_symbols.sql
│       ├── V6__add_top50_market_cap_symbols.sql
│       └── V7__update_us_symbols.sql
├── build.gradle                       # Gradle 빌드 설정
├── docker-compose.yml                 # MySQL + Redis 컨테이너
└── README.md

```
## 📡 API 엔드포인트

### - 인증 (Authentication)

| Method | Endpoint        | 설명         | 
| ------ | --------------- | ------------| 
| POST   | `/auth/signup`  | 회원가입      | 
| POST   | `/auth/login`   | 로그인       | 
| POST   | `/auth/logout`  | 로그아웃      | 
| POST   | `/auth/refresh` | 토큰 갱신     | 
| GET    | `/auth/me`      | 내 정보 조회  | 

### - 종목 (Symbol)

| Method | Endpoint            | 설명                   | 
| ------ | ------------------- | --------------------- | 
| GET    | `/api/symbols`      | 종목 검색 (market, q)   | 
| GET    | `/api/symbols/{id}` | 종목 상세 조회           | 

### - 관심종목 (Watchlist)

| Method | Endpoint                    | 설명              |
| ------ | --------------------------- | ---------------- | 
| GET    | `/api/watchlist`            | 내 관심종목 목록     |
| POST   | `/api/watchlist`            | 관심종목 추가       | 
| DELETE | `/api/watchlist/{symbolId}` | 관심종목 삭제       | 

### - 시세 (Stock Data)

| Method | Endpoint                          | 설명                  | 
| ------ | --------------------------------- | -------------------- | 
| GET    | `/api/stocks/id/{id}/detail`      | 상세 시세 조회          | 
| GET    | `/api/stocks/id/{id}/chart/daily` | 일봉 차트 (from, to)   | 

### - 시장 (Market)

| Method | Endpoint                        | 설명                | 
| ------ | ------------------------------- | ------------------ |
| GET    | `/api/markets/domestic/ranking` | 국내 시가총액 순위     |

### - 관리자 (Admin)

| Method | Endpoint                | 설명              | 
| ------ | ----------------------- | ---------------- | 
| GET    | `/admin/health`         | 시스템 헬스 체크     | 
| POST   | `/admin/quotes/refresh` | 시세 수동 갱신      | 
