# 攔截器與提供者架構 (Interceptor & Provider)

## 概述
攔截器模組負責處理 HTTP 請求發送前與回應後的各種「橫切關注點 (Cross-Cutting Concerns)」，例如：認證標頭注入、重試機制、全域日誌、分散式追蹤 (Tracing) 等。為了達到模組化，本系統採用了 **Provider Pattern**。

## 核心介面：RestClientInterceptorProvider
```java
public interface RestClientInterceptorProvider {
    boolean supports(String systemName);
    void apply(RestClient.Builder builder, String systemName);
}
```

## 職責分離
本架構將「攔截器本身」與「攔截器的掛載邏輯」拆分為兩個類別：
1.  **Interceptor (攔截器)**：實作 `ClientHttpRequestInterceptor`，專注於處理 Request/Response（如 `LoggingInterceptor`、`AuthenticationInterceptor`）。屬於**純粹的 HTTP 處理邏輯**。
2.  **Provider (提供者)**：實作 `RestClientInterceptorProvider`，專注於決定**是否要掛載**、**如何建構**該攔截器，並套用到 `RestClient.Builder` 上。

| 提供者 (Provider) | 實際攔截器 (Interceptor) | 用途與職責 |
| :--- | :--- | :--- |
| [TracingInterceptorProvider](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/provider/impl/TracingInterceptorProvider.java) | [TracingInterceptor](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/interceptor/TracingInterceptor.java) | 負責在外部 HTTP 請求加上 `X-Trace-Id` 標頭，確保微服務之間的請求鏈路 (Trace) 可以被追蹤與串接。 |
| [AuthenticationInterceptorProvider](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/provider/impl/AuthenticationInterceptorProvider.java) | [AuthenticationInterceptor](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/interceptor/AuthenticationInterceptor.java) | 讀取系統設定中的 `auth-type`，並交由 `AuthStrategy` 在 Request 中動態注入 `Authorization` 標頭 (如 JWT Token 或是 Basic Auth)。 |
| [RetryInterceptorProvider](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/provider/impl/RetryInterceptorProvider.java) | [RetryInterceptor](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/interceptor/RetryInterceptor.java) | 根據 properties 中的設定 (如 `max-attempts`, `delay-millis`)，在遭遇外部連線失敗或超時錯誤時，自動進行重試邏輯。 |
| [LoggingInterceptorProvider](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/provider/impl/LoggingInterceptorProvider.java) | [LoggingInterceptor](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/interceptor/LoggingInterceptor.java) | 負責攔截並記錄外部呼叫的 HTTP Request (URL, Headers, Method) 與 Response (Status Code, Body)，提供統一的 Log 格式以利除錯。 |
| [CustomHeaderInterceptorProvider](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/provider/impl/CustomHeaderInterceptorProvider.java) | [CustomHeaderInterceptor](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/interceptor/CustomHeaderInterceptor.java) | 讀取 properties 裡自訂的 `external.auth-headers.[systemName]` 設定，並透過 `HeaderValueResolver` 將動態標頭 (如 `${uuid}`) 解析後塞入 Request。 |
| [ErrorInterceptorProvider](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/provider/impl/ErrorInterceptorProvider.java) | [ExternalApiExceptionInterceptor](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/interceptor/ExternalApiExceptionInterceptor.java) | 負責攔截 HTTP 狀態碼為 4xx / 5xx 的伺服器與客戶端錯誤，並透過 `ExternalApiErrorHandler` 將其轉譯為系統內的 `ExternalApiException`。 |
| [BusinessErrorInterceptorProvider](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/provider/impl/BusinessErrorInterceptorProvider.java) | [BusinessErrorInterceptor](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/interceptor/BusinessErrorInterceptor.java) | 負責攔截 HTTP 狀態碼為 2xx 但「回傳 Body 中帶有業務錯誤代碼」的情境，透過 `BusinessErrorStrategy` 解析 Body 並拋出對應的業務例外。 |

## 執行順序控制
由於攔截器有嚴格的執行順序要求（例如：必須先重試，再記錄 Log），各個 Provider 透過 Spring 的 `@Order` 註解來定義優先級。`RestClientFactory` 在套用時會依賴 `OrderComparator` 來確保執行順序正確無誤。
