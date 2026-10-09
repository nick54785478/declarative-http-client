# Declarative HTTP Client Module

## External HTTP Client Architecture

### 相關技術選用 (Tech Stack)

* **語言與底層框架**：Java 21 / Spring Boot 4.0.2
* **核心 HTTP 發送引擎**：`RestClient` (Spring 6 推出之流暢式 HTTP 客戶端，正式取代舊有的 `RestTemplate`)
* **宣告式介面綁定**：`Spring HTTP Interfaces` (利用 `@GetExchange`、`@PostExchange` 宣告 API 介面，搭配動態代理 `HttpServiceProxyFactory` 產生實體，達成零實作呼叫)
* **API 規格與文件化**：`OpenAPI (Swagger)` (用於標準化並視覺化展示本專案對外提供的 API 介面)

---

### 設計目標

本模組提供一套**可擴充、可設定化、可插拔、多系統支援、符合企業級設計標準**的外部系統呼叫架構，徹底解決：

* **多外部系統整合**：將各個外部系統隔離於 `external/` 目錄。
* **不同認證方式**：支援 JWT / Basic / None 驗證機制。
* **統一錯誤處理**：攔截 HTTP 4xx/5xx 以及業務錯誤（Business Error，如 2xx 但 body 包含錯誤碼），並轉換為統一的 `ExternalApiException`。
* **自訂動態 Header 支援**：支援注入靜態 Header 或動態變數（如 `${uuid}`, `${timestamp}`）。
* **統一 TraceId 管理**：寫入 Header 與 MDC 日誌。
* **統一 Logging**：記錄完整的 Request / Response 歷程。
* **統一 Retry 機制**：針對 5xx 等特定錯誤進行短暫重試。
* **零修改核心擴充**：未來擴充新系統，核心基礎建設 (`httpclient/`) 完全不需要修改。

---

### 架構總覽

```text
ExternalSystemProperties (設定檔注入)
        ↓
RestClientFactory                   --> 核心工廠，針對各個 systemName 初始化 RestClient.Builder
        ↓
RestClientInterceptorProvider (多個)  --> 透過 Spring DI，將各種 Feature (auth, error, header 等) 的攔截器掛載至 Builder
        ↓
HttpServiceProxyFactory             --> Spring 6 HTTP Interfaces 動態代理工廠
        ↓
XXXHttpClient Interface             --> 強型別的宣告式客戶端 (Declarative Client)
```

---

### 目錄結構 (Package by Feature / System)

本模組遵循 **Clean Architecture** 與 **Package by Feature** 的精神，將目錄區分為兩大塊：

1. **`infra/httpclient/` (純技術底層)**
   * `core/`: 核心元件（如 `RestClientFactory`）。
   * `feature/`: 將攔截器與提供者依「功能」分裝。
     * `auth/`: 認證攔截器與策略。
     * `error/`: 統一錯誤攔截與解析。
     * `header/`: 動態 Header 解析。
     * `logging/`: 統一日誌。
     * `retry/`: 重試機制。
     * `tracing/`: TraceId 追蹤。

2. **`infra/external/` (外部系統實作)**
   * `authsystem/`: Auth 系統的具體 API 宣告（`AuthHttpClient`）與應用層適配器。
   * *(未來其他系統可直接橫向擴充)*

---

### 核心元件說明

**1. RestClientFactory**
* **職責**：根據 `systemName` 建立帶有所有共用攔截器的 `RestClient`。
* **特點**：底層無需感知有多少種攔截器，只要實作了 `RestClientInterceptorProvider` 都會被自動註冊。

**2. RestClientInterceptorProvider**
* **介面**：
  ```java
  public interface RestClientInterceptorProvider {
      boolean supports(String systemName);
      void apply(RestClient.Builder builder, String systemName);
  }
  ```
* **特點**：各個 Feature (`auth`, `error`, `logging` 等) 都有自己的 Provider，透過 `@Order` 控制掛載順序，符合 OCP（開放封閉原則）。

**3. Error Handling (錯誤處理機制)**
* **HTTP 異常 (`ExternalApiExceptionInterceptor`)**：處理 4xx / 5xx 狀態碼。
* **業務異常 (`BusinessErrorInterceptor`)**：處理狀態碼 2xx 但 JSON body 內含失敗碼的情況。
* 兩者皆透過 Registry 動態尋找對應系統的錯誤策略 (Strategy / Handler) 來轉換為 `ExternalApiException`。

**4. ExternalSystemProperties**
* **配置** (於 `application.properties` 或 `application.yml`)：
  ```properties
  external.retry.max-attempts=3
  external.retry.delay-millis=1000
  
  # AuthService 系統自定義
  external.systems.auth.auth-type=JWT
  external.systems.auth.base-url=http://localhost:8088
  external.systems.auth.enable-business-error-handling=true
  
  # 自訂 Header (支援動態變數)
  external.custom-headers.auth.X-Client-Id=my-client
  external.custom-headers.auth.X-Request-Id=${uuid}
  ```

**5. Declarative HttpClient (Spring 6)**
* **範例**：
  ```java
  public interface AuthHttpClient {
      @PostExchange("/api/v1/login")
      JwTokenGettenData login(@RequestBody GetJwTokenCommand command);
  }
  ```
* **優點**：業務層完全不需要處理 HTTP 細節（如 JSON 轉換、Header 注入、狀態碼判斷），像呼叫本地方法一樣呼叫外部 API。

---

### 架構優點與 Design Pattern 應用

1. **高內聚低耦合**
   * **Factory** 負責建立 Client。
   * **Provider** 負責注入橫切功能 (Cross-cutting concerns)。
   * **Strategy** 負責特定的演算法 (如認證、錯誤判斷)。
   * **Registry** 負責管理與分發對應系統的處理器。

2. **符合 SOLID 原則**
   * **SRP (單一職責)**：每個 package 只負責一件事。
   * **OCP (開放封閉)**：新增功能或新系統，只需新增檔案，不需修改既有程式碼。
   * **DIP (依賴反轉)**：依賴於抽象介面 (`AuthHttpClient`) 而非具體實作。

3. **Design Pattern 應用**
   * **Strategy Pattern** (認證策略、業務錯誤策略)
   * **Factory Pattern** (`RestClientFactory`, `HeaderValueResolverFactory`)
   * **Chain of Responsibility** (透過多個 Interceptor 串聯)
   * **Proxy / Facade Pattern** (`HttpServiceProxyFactory`)
