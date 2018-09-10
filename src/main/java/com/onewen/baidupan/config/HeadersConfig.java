package com.onewen.baidupan.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.onewen.baidupan.util.HttpUtil;
import com.onewen.baidupan.util.LoadConfig;

import okhttp3.HttpUrl;

/**
 * 头部信息配置
 * 
 * @author 梁光运
 * @date 2018年8月26日
 */
public class HeadersConfig {
	
	private static Map<String, HeadersConfig> configs;

	private String url;

	private Map<String, String> headers;

	public HeadersConfig(String url, Map<String, String> headers) {
		this.url = url;
		this.headers = headers;
	}

	public String getUrl() {
		return url;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}
	
	public static Map<String, String> getConfigHeaders(String url) {
		HeadersConfig config = configs.get(HttpUrl.parse(url).host());
		return config != null ? new HashMap<>(config.headers) : new HashMap<>();
	}

	@LoadConfig
	private static void init(List<HeadersConfig> list) {
		Map<String, HeadersConfig> map = new HashMap<>();
		for (HeadersConfig config : list) {
			HttpUtil.addHostHeaders(config.url, config.headers);
			map.put(HttpUrl.parse(config.url).host(), config);
		}
		configs = map;
	}

}
