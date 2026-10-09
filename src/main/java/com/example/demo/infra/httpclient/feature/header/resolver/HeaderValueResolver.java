package com.example.demo.infra.httpclient.feature.header.resolver;

/**
 * Header 值解析器介面。
 *
 * <p>
 * 負責解析 HTTP Header 設定的值，例如：
 * <ul>
 * <li>${uuid} 轉換為動態 UUID</li>
 * <li>${timestamp} 轉換為動態時間戳</li>
 * <li>靜態字串 則保持原值</li>
 * </ul>
 * </p>
 *
 * <p>
 * 實作類別需透過 {@link #supports(String)} 判斷該值是否支援解析， 再由 {@link #resolve(String)}
 * 回傳最終值。
 * </p>
 */
public interface HeaderValueResolver {

	/**
	 * 判斷該 resolver 是否支援解析目前的 Header 值。
	 *
	 * @param value 待解析的 Header 值，可能包含動態表達式
	 * @return true 若支援解析, false 若忽略
	 */
	boolean supports(String value);

	/**
	 * 解析動態 Header 值。
	 *
	 * @param value 待解析的 Header 值
	 * @return 解析後的最終字串
	 */
	String resolve(String value);
}
