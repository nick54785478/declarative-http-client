# Infrastructure 架構重構 (Infra Architecture Redesign)

## 1. 原有架構痛點
目前我們的 infra.client 這個 package 採用的是**「純技術分層 (Package by Layer)」**。包含 interceptor、provider、egistry、dapter 等。

這種分層方式在規模擴大時，會產生以下痛點：
*   **低內聚 (Low Cohesion)**：修改單一功能（如錯誤處理）時，須橫跨多個 package (error, interceptor, provider, egistry)，導致程式碼修改極為發散。
*   **底層與業務邏輯混雜 (Mixed Concerns)**：共用的 HTTP 基礎建設（如 RestClientFactory）與特定外部系統的業務邏輯（如 AuthHttpClient, AuthClientAdapter）混雜在同一個 client 目錄下，這違反了 Clean Architecture 中 Adapter 職責分離的原則。

---

## 2. 新架構藍圖：依功能與系統分裝 (Package by Feature / System)
為了解決上述痛點，我們將 infra.client 大幅重構為：
1.  **httpclient**：純粹、與業務無關的技術 HTTP 客戶端基礎建設。
2.  **external**：與外部系統介接的實作 (Outbound Adapters)。

### 目錄結構與詳細說明
`	ext
com.example.demo.infra
├── httpclient/                  [1. 純粹 HTTP 客戶端基礎建設]
│   ├── core/                    (核心元件)
│   │   └── RestClientFactory.java (負責產出共用的 RestClient 實體)
│   │
│   └── feature/                 (將 Interceptor, Provider, Strategy 等「依功能」分裝)
│       ├── auth/                (將 AuthStrategy, Interceptor, Provider 聚在一起)
│       ├── error/               (將 Handler, Registry, Interceptor, Provider 等錯誤處理聚在一起)
│       ├── header/              (將 Resolver, Interceptor, Provider 等動態標頭聚在一起)
│       ├── logging/             (日誌攔截器與 Provider)
│       ├── retry/               (重試攔截器與 Provider)
│       └── tracing/             (追蹤攔截器與 Provider)
│
└── external/                    [2. 外部系統介接實作]
    ├── authsystem/              (Auth 系統專屬的介接目錄)
    │   ├── AuthHttpClient.java  (Declarative Client 介面，定義外部 API 規格)
    │   └── AuthClientAdapter.java (實作 Application 層 AuthSystemPort 的轉接器)
    │
    └── paymentsystem/           (未來可新增的其他系統...)
`

### 重構設計重點說明
* **特徵打包 (Package by Feature)**：在 httpclient/feature 底下，不再依照類別的屬性 (如都是 interceptor) 進行分類，而是依照「它解決什麼問題」來分類。例如，所有跟「錯誤處理」有關的策略、攔截器、提供者與註冊表，全部都在 error 包中。
* **強型別介面 (Declarative Client)**：利用 Spring 6 的 HTTP Interfaces，將外部 API 定義在 AuthHttpClient 等介面中，配合 HttpServiceProxyFactory 動態產生實作，大幅減少樣板程式碼。

---

## 3. 架構效益 (Benefits)

1.  **高內聚、好維護 (High Cohesion)**
    *   以「功能 (Feature)」為單位組織程式碼。例如 httpclient/feature/header 資料夾內，就完整包含了攔截器、工廠與所有的解析器實作。開發者修改單一功能時不再需要跨資料夾跳躍。
2.  **純粹的技術底層 (Clean Infrastructure)**
    *   httpclient 資料夾徹底與業務脫鉤，它完全不知道 Auth 或 Payment 系統的存在。未來甚至可以將整個 httpclient 打包成獨立的共用 Library (.jar) 供其他微服務使用。
3.  **無痛橫向擴充 (Scalability of Adapters)**
    *   未來每串接一個新的外部系統，只需在 external/ 底下新增一個對應的資料夾。各個外部系統的 Adapter 互相隔離，完美契合 Clean Architecture 提倡的插件式架構 (Plug-in Architecture)。
