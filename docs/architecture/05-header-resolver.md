# 動態標頭解析架構 (Header Resolver)

## 概述
在串接外部 API 時，有時必須在 Header 中帶入動態產生的值，例如每次請求不重複的 UUID (`X-Request-Id`)，或者是當前的 Unix 時間戳 (`X-Timestamp`)。
為了解決在 Properties 檔案中無法撰寫動態程式碼的痛點，本模組實作了**自訂標頭攔截與動態解析機制**。

## 核心設計
透過 [CustomHeaderInterceptor](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/interceptor/CustomHeaderInterceptor.java) 在發送請求前，從設定檔 [ExternalSystemProperties](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/shared/properties/ExternalSystemProperties.java) 中讀取為該系統定義的所有自訂標頭，並經由 [HeaderValueResolverFactory](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/header/factory/HeaderValueResolverFactory.java) 解析後寫入 HTTP Request。

### HeaderValueResolverFactory
負責管理所有實作了 `HeaderValueResolver` 介面的解析器。當接收到一個標頭的值時，工廠會依序詢問所有解析器是否支援解析該值，並呼叫第一個支援的解析器進行轉換。

### HeaderValueResolver 介面 ([HeaderValueResolver](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/header/resolver/HeaderValueResolver.java))
```java
public interface HeaderValueResolver {
    boolean supports(String value);
    String resolve(String value);
}
```

## 現有實作策略
| 解析器名稱 | 支援語法 | 說明 |
| :--- | :--- | :--- |
| [UuidHeaderValueResolver](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/header/resolver/impl/UuidHeaderValueResolver.java) | `${uuid}` | 當設定檔中的標頭值設定為 `${uuid}` 時觸發，自動生成一組隨機的 UUID 取代該值。 |
| [StaticHeaderValueResolver](file:///d:/桌面/SpringBootWorkSpace/http-interface-demo/src/main/java/com/example/demo/infra/client/header/resolver/impl/StaticHeaderValueResolver.java) | `(不以 ${ 開頭的字串)` | 最基礎的解析器，若無特殊佔位符，則原封不動地回傳設定檔中的靜態字串。 |

## 優勢
若未來外部系統要求加入 `${timestamp}`、`${signature}` 甚至是 `${tenantId}` 等動態標頭，我們僅需實作對應的 `HeaderValueResolver` 並註冊為 Spring Bean，即可瞬間賦予系統強大的動態標頭解析能力，完美符合開放封閉原則 (OCP)。
