# 錯誤處理與註冊表架構 (Error Handling & Registry)

## 概述
在微服務架構中，呼叫外部 API 時必定會遇到兩種情境的錯誤：
1.  **HTTP 狀態碼錯誤**：HTTP 4xx (Client Error) 或 HTTP 5xx (Server Error)。
2.  **業務邏輯錯誤**：HTTP 狀態碼為 200，但 Response Body 內部定義了業務錯誤代碼 (如 `{"code":"9999", "msg":"餘額不足"}`)。

本模組利用攔截器捕捉這些錯誤，並透過 **Registry Pattern (註冊表模式)** 動態分派錯誤解析器，最終轉換為統一的領域例外 `ExternalApiException`。

## 核心機制

### 1. 統一例外轉換 (ExternalApiException)
所有的外部錯誤，無論是 HTTP 層級還是業務層級，最終都會被拋出為 `ExternalApiException`（已上推至 `application.shared.exception`）。展示層的 `GlobalExceptionHandler` 可無縫捕捉此例外並進行處理。

### 2. HTTP 錯誤處理 ([ExternalApiErrorHandler](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/error/handler/ExternalApiErrorHandler.java))
*   **攔截器**：[ExternalApiExceptionInterceptor](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/interceptor/ExternalApiExceptionInterceptor.java)。
*   **註冊表**：[ExternalApiErrorHandlerRegistry](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/registry/ExternalApiErrorHandlerRegistry.java)。
*   **職責**：當 HTTP 回應非 2xx 時，攔截器會將 Response 拋給註冊表中對應該系統的 `ExternalApiErrorHandler`。
*   **退路機制 (Fallback)**：若未註冊專屬的錯誤處理器，將使用 [DefaultExternalApiErrorHandler](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/error/handler/impl/DefaultExternalApiErrorHandler.java) 進行標準的錯誤封裝。
*   **其他實作**：針對特定系統可實作專屬 Handler，如 [AuthErrorHandler](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/error/handler/impl/AuthErrorHandler.java)。

### 3. 業務錯誤處理 ([BusinessErrorStrategy](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/error/business/BusinessErrorStrategy.java))
*   **攔截器**：[BusinessErrorInterceptor](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/interceptor/BusinessErrorInterceptor.java)。
*   **註冊表**：[BusinessErrorStrategyRegistry](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/registry/BusinessErrorStrategyRegistry.java)。
*   **職責**：當 HTTP 回應為 2xx 時，攔截器會讀取 Body，交由 `BusinessErrorStrategy` 判斷是否有業務錯誤。若有，則從 Body 擷取錯誤代碼與訊息，並拋出 `ExternalApiException`。
*   **退路機制 (Fallback)**：預設提供 [DefaultBusinessErrorStrategy](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/error/business/impl/DefaultBusinessErrorStrategy.java) (通常直接回傳無業務錯誤)。
*   **其他實作**：針對具有自訂 Body 錯誤格式的系統，可實作專屬 Strategy，如 [AuthBusinessErrorStrategy](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/error/business/impl/AuthBusinessErrorStrategy.java)。

## 架構效益
*   不同外部系統的錯誤 JSON 結構五花八門，透過 `Registry` 機制，可以為每個系統訂製專屬的 `ErrorHandler` 與 `BusinessErrorStrategy`，避免在攔截器內寫滿系統名稱判斷。
*   保護系統核心業務層，確保業務層只會收到強型別且標準化的 `ExternalApiException`，不必去 Parse 外部的爛字串。
