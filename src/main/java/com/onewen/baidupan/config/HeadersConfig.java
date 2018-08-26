package com.onewen.baidupan.config;

import java.util.List;
import java.util.Map;

import com.onewen.baidupan.util.HttpUtil;
import com.onewen.baidupan.util.LoadConfig;

/**
 * 头部信息配置
 * 
 * @author 梁光运
 * @date 2018年8月26日
 */
public class HeadersConfig {

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

	@LoadConfig
	private static void init(List<HeadersConfig> configs) {
		for (HeadersConfig config : configs) {
			HttpUtil.addHostHeaders(config.url, config.headers);
		}
	}

}
