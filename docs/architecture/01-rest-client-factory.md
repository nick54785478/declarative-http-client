# RestClient 核心工廠架構 (Rest Client Factory)

## 概述
`RestClientFactory` 是整個外部 HTTP 客戶端模組的入口點。它的主要職責是根據外部系統名稱 (systemName)，讀取設定檔並組裝出具備完整攔截器與認證機制的 `RestClient`。

## 核心設計理念
*   **集中化管理**：所有的外部 HTTP 客戶端 (HTTP Interface) 皆由統一的 Factory 產生，避免了分散各處的 RestTemplate 或 WebClient 建置邏輯。
*   **開放封閉原則 (OCP)**：Factory 核心不包含任何具體的攔截邏輯，而是透過尋訪 `RestClientInterceptorProvider` 來動態掛載功能。未來新增功能（例如：Cache 攔截器）完全不需要修改 Factory 本身。
*   **支援動態擴充**：完全依賴 `ExternalSystemProperties`，新增外部系統只需要在 `application.properties` 中加幾行設定。

## 參與元件
1.  **RestClientFactory** (`infra.client.factory.RestClientFactory`)
    *   負責初始化 `RestClient.Builder`。
    *   讀取 `baseUrl`。
    *   負責排序並套用支援該系統的 Providers。
2.  **ExternalSystemProperties** (`infra.shared.properties.ExternalSystemProperties`)
    *   定義於設定檔中的多系統組態 (支援 `base-url`, `auth-type`, `retry`, `headers` 等)。
3.  **Spring 6 HttpServiceProxyFactory**
    *   在產生 `RestClient` 後，系統會透過 `HttpServiceProxyFactory` 將其轉換為強型別的 Java Interface（如 `AuthHttpClient`），讓應用層可以像呼叫本地方法一樣呼叫外部 API。

## 運作流程
1. 傳入 `systemName` (例如 `"auth"`) 給 `RestClientFactory.create()`。
2. Factory 從 Properties 中讀取該系統的 `baseUrl`。
3. Factory 尋訪所有實作了 `RestClientInterceptorProvider` 的 Bean。
4. 若 Provider 的 `supports(systemName)` 回傳 true，則呼叫 `apply(builder, systemName)`，將特定的 Interceptor 掛載到 Builder 上。
5. 最終回傳配置完畢的 `RestClient`。
