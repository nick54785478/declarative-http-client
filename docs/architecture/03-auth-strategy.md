# 認證策略架構 (Authentication Strategy)

## 概述
外部系統的認證方式五花八門（Token、Basic Auth、無認證等）。為了避免在攔截器內撰寫義大利麵條般的 `if-else` 或 `switch-case` 判斷式，本模組採用了 **Strategy Pattern (策略模式)** 與 **Factory Pattern (工廠模式)** 進行解耦。

## 核心設計
1.  **AuthStrategy 介面** ([AuthStrategy](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/auth/strategy/AuthStrategy.java))
    *   定義所有的認證策略必須實作 `supports(authType)` 判斷是否支援，以及 `apply(request, config)` 執行實際的標頭注入。
2.  **AuthStrategyFactory** ([AuthStrategyFactory](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/auth/factory/AuthStrategyFactory.java))
    *   集中管理所有的 `AuthStrategy` Bean。
    *   在建立 HTTP 客戶端時，根據設定檔的 `auth-type` (例如：`JWT`)，動態取得對應的策略實例。

## 現有策略實作
| 策略名稱 | AuthType 名稱 | 說明 |
| :--- | :--- | :--- |
| [JwtAuthStrategy](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/auth/strategy/impl/JwtAuthStrategy.java) | `JWT` | 從 `ContextHolder` 中取得目前使用者的 JWT Token，並加上 `Bearer ` 前綴後放入 `Authorization` 標頭中。 |
| [BasicAuthStrategy](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/auth/strategy/impl/BasicAuthStrategy.java) | `BASIC` | 根據設定檔中的帳號密碼進行 Base64 編碼，並加上 `Basic ` 前綴後放入 `Authorization` 標頭。 |
| [NoAuthStrategy](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/auth/strategy/impl/NoAuthStrategy.java) | `NONE` | 不執行任何認證動作，適用於公開的外部 API。 |

## 優勢
*   **隔離性高**：每一種認證邏輯獨立封裝，互不干擾。
*   **易於擴充**：若未來需要串接 OAuth2 系統，只需新增一個 `OAuth2AuthStrategy` 實作並註冊為 Bean，完全不需修改既有程式碼。
