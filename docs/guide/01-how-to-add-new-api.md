# 從零到一：如何介接一個新的外部微服務 API

這份說明書將引導您如何在 `declarative-http-client` 架構中，標準化地新增一個外部系統的 API 呼叫。

本架構的核心精神是 **「宣告式介面、零底層實作、高度可設定化」**。以下我們將以目前專案中真實配置的 **AuthService (驗證系統)** 為例，為您逐步解析！

---

## 步驟 1：定義外部系統的連線屬性

首先，在 `application.properties` (或 `application.yml`) 中定義您要介接的外部系統連線資訊。

以 AuthService 為例，其系統代碼為 `auth`：

```properties
# 定義系統名稱為 auth 的基礎設定
external.systems.auth.base-url=http://localhost:8088
external.systems.auth.auth-type=JWT # 支援 JWT, BASIC, NONE

# (選用) 是否啟用該系統專屬的「業務邏輯錯誤攔截」 (Business Error Handling)
external.systems.auth.enable-business-error-handling=true

# (選用) 如果該系統需要特定的靜態 Header，可以直接設定
external.systems.auth.headers.X-Client-Id=my-client
external.systems.auth.headers.X-Env=dev
```

---

## 步驟 2：定義 Application 層的 Outbound Port

為了遵守 Clean Architecture (整潔架構)，請在 `application/port/out/` 底下，定義好與外部系統互動的業務介面。這個介面是 Application 層用來跟外部溝通的合約，不應該包含任何與 HTTP 或外部系統相關的技術細節。

```java
package com.example.demo.application.port.out;

public interface AuthServiceClientPort {

    // 定義登入的業務合約
    JwTokenGottenResult login(GetJwTokenFromAuthServiceCommand command);

    // 定義取得使用者資訊的業務合約
    UserInfoGottenResult getUserById(Long id);
}
```

---

## 步驟 3：宣告 Infra 層的 HTTP 介面 (Declarative Client)

接著在基礎建設層 (Infra Layer) 的 `infra/external/authsystem/` 底下，建立對應該系統的 HTTP 介面。您 **不需要寫任何連線的實作程式碼**。

```java
package com.example.demo.infra.external.authsystem;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

public interface AuthHttpClient {

    // 呼叫登入端點
    @PostExchange("/api/v1/login")
    JwTokenGottenResult login(@RequestBody GetJwTokenFromAuthServiceCommand command);

    // 呼叫取得使用者資訊端點
    @GetExchange("/api/v1/users/{id}")
    UserInfoGottenResult getUserById(@PathVariable Long id);
}
```

---

## 步驟 4：在設定檔中註冊 HTTP Client 為 Bean

為了讓 Spring 知道如何動態產生實體並注入您的 `AuthHttpClient`，請前往 `config/HttpClientConfiguration.java` 新增一個 `@Bean`：

```java
@Bean
public AuthHttpClient authHttpClient(RestClientFactory factory) {
    // 傳入的字串 "auth" 必須完全符合 application.properties 中的 external.systems.auth
    return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(factory.create("auth")))
            .build()
            .createClient(AuthHttpClient.class);
}
```
> **黑科技**：`RestClientFactory` 會在底層自動幫這個 `auth` 系統綁定好 **Token認證、MDC TraceId、日誌輸出、重試機制 (Retry)** 與 **統一錯誤處理**，您什麼都不用操心！

---

## 步驟 5：建立 Outbound Adapter 封裝基礎建設

請在相同的 `infra/external/authsystem/` 資料夾下，建立一個 Adapter 來實作 Application 層所定義的 Port。這是打通業務層與技術層的關鍵橋樑：

```java
package com.example.demo.infra.external.authsystem;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthClientAdapter implements AuthServiceClientPort {

    private final AuthHttpClient authHttpClient;

    @Override
    public JwTokenGottenResult login(GetJwTokenFromAuthServiceCommand command) {
        // 直接呼叫 declarative client，完美隔離了基礎建設的 HTTP 實作細節
        return authHttpClient.login(command);
    }

    @Override
    public UserInfoGottenResult getUserById(Long id) {
        return authHttpClient.getUserById(id);
    }
}
```

## 步驟 6：實作系統專屬的 HTTP 錯誤解析 (4xx / 5xx)

當外部系統回傳真正的 HTTP 錯誤 (如 `400 Bad Request` 或 `500 Internal Server Error`) 時，我們需要將對方的錯誤格式轉換為系統內部的標準例外 (`ExternalApiException`)。

首先，根據對方的錯誤 JSON 結構，定義一個錯誤回應的 DTO：

```java
package com.example.demo.infra.httpclient.feature.error.res.impl;

import com.example.demo.infra.httpclient.feature.error.res.ExceptionResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthExceptionResponse implements ExceptionResponse {
    private String code;
    private String message;
}
```

接著，實作 `ExternalApiErrorHandler` 來攔截並轉換這些 HTTP 錯誤：

```java
package com.example.demo.infra.httpclient.feature.error.handler.impl;

import org.springframework.stereotype.Component;

@Component
public class AuthErrorHandler implements ExternalApiErrorHandler {

    private final ObjectMapper objectMapper;
    // ... constructor omitted for brevity ...

    @Override
    public boolean supports(String systemName) {
        return "auth".equalsIgnoreCase(systemName); // 綁定 auth 系統
    }

    @Override
    public ExternalApiException handle(String systemName, String traceId, ClientHttpResponse response, String responseBody) {
        try {
            // 將原始的 responseBody 轉換為對方的錯誤 DTO
            AuthExceptionResponse errorRes = objectMapper.readValue(responseBody, AuthExceptionResponse.class);
            // 包裝並拋出統一的 ExternalApiException
            return new ExternalApiException(systemName, response.getStatusCode().value(), errorRes.getCode(), errorRes.getMessage(), traceId);
        } catch (Exception e) {
            // 如果解析失敗，就提供一個預設錯誤
            return new ExternalApiException(systemName, response.getStatusCode().value(), "AUTH_SYSTEM_ERROR", responseBody, traceId);
        }
    }
}
```

---

## 步驟 7 (進階選配)：實作系統專屬的業務錯誤解析 (2xx)

有時候，對方的 API 總是回傳 `HTTP 200 OK`，但裡面卻包著業務錯誤 (例如 `{"code": "ERR_001", "message": "密碼錯誤"}`)。
因為您在步驟 1 設定了 `enable-business-error-handling=true`，您可以實作一個專屬的 Strategy 來攔截它：

```java
package com.example.demo.infra.httpclient.feature.error.business.impl;

import org.springframework.stereotype.Component;

@Component
public class AuthBusinessErrorStrategy implements BusinessErrorStrategy {

    @Override
    public boolean supports(String systemName) {
        return "auth".equals(systemName); // 綁定 auth 系統
    }

    @Override
    public void checkAndThrow(HttpRequest request, ClientHttpResponse response, String body) {
        // 解析 body
        JsonNode jsonNode = JsonParseUtil.parseNode(body);
        if (jsonNode != null && jsonNode.has("code") && !"200".equals(jsonNode.get("code").asText())) {
            // 拋出統一的 ExternalApiException，後續將交由 GlobalExceptionHandler 處理
            throw new ExternalApiException("auth", "Auth 系統發生業務邏輯錯誤", body);
        }
    }
}
```

---

## 🎉 恭喜完成！
現在您的 Application 層 (如 `AuthUseCase`) 只要注入 `AuthServiceClientPort` 就能開心地呼叫外部資料。這支 API 自動具備了：
1. 自動注入 `auth-type=JWT` 的授權邏輯
2. 自動注入自訂 Header (`X-Client-Id`, `X-Env`) 以及 MDC TraceId
3. 自動印出完整的 Request / Response 歷程與耗時
4. 遇到 HTTP 5xx 或連線異常自動觸發 Retry
5. **不管是 HTTP 4xx/5xx 或是偽裝成 200 的業務錯誤**，最終都會被 GlobalExceptionHandler 統一攔截並轉化為標準錯誤回應格式！
