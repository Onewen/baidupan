package com.onewen.baidupan.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

/**
 * Cookie管理
 * 
 * @author 梁光运
 * @date 2018年8月22日
 */
public class CookieStore {

	private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

	public static final class CookieInfo {
		private String name;
		private String value;
		private long expiresAt;
		private String domain;
		private String path;
		private boolean secure;
		private boolean httpOnly;
		private boolean hostOnly;

		public CookieInfo(String name, String value, long expiresAt, String domain, String path, boolean secure,
				boolean httpOnly, boolean hostOnly) {
			this.name = name;
			this.value = value;
			this.expiresAt = expiresAt;
			this.domain = domain;
			this.path = path;
			this.secure = secure;
			this.httpOnly = httpOnly;
			this.hostOnly = hostOnly;
		}

		public static CookieInfo parse(Cookie cookie) {
			return new CookieInfo(cookie.name(), cookie.value(), cookie.expiresAt(), cookie.domain(), cookie.path(),
					cookie.secure(), cookie.hostOnly(), cookie.hostOnly());
		}

		public Cookie toCookie() {
			Cookie.Builder builder = new Cookie.Builder();
			builder.name(name).value(value).expiresAt(expiresAt).path(path);
			if (this.secure)
				builder.secure();
			if (this.hostOnly)
				builder.httpOnly();
			if (hostOnly)
				builder.hostOnlyDomain(domain);
			else
				builder.domain(domain);
			return builder.build();
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		public long getExpiresAt() {
			return expiresAt;
		}

		public String getDomain() {
			return domain;
		}

		public String getPath() {
			return path;
		}

		public boolean isSecure() {
			return secure;
		}

		public boolean isHttpOnly() {
			return httpOnly;
		}

		public boolean isHostOnly() {
			return hostOnly;
		}

		@Override
		public String toString() {
			return "CookieInfo [name=" + name + ", value=" + value + ", expiresAt=" + expiresAt + ", domain=" + domain
					+ ", path=" + path + "]";
		}

	}

	/**
	 * 获取cookie
	 * 
	 * @param httpUrl 连接地址
	 * @return
	 */
	public List<Cookie> getCookie(HttpUrl httpUrl) {
		return cookieStore.get(httpUrl.host());
	}

	/**
	 * 获取cookie
	 * 
	 * @param url 连接地址
	 * @return
	 */
	public List<Cookie> getCookie(String url) {
		return getCookie(HttpUrl.parse(url));
	}

	/**
	 * 获取cookie
	 * 
	 * @param url 连接地址
	 * @return
	 */
	public Cookie getCookie(String url, String name) {
		List<Cookie> cookies = cookieStore.get(HttpUrl.parse(url).host());
		if (cookies == null)
			return null;
		for (Cookie cookie : cookies) {
			if (cookie.name().equals(name))
				return cookie;
		}
		return null;
	}

	/**
	 * 添加cookie
	 * 
	 * @param httpUrl 连接地址
	 * @param cookies 列表
	 */
	public void addCookie(HttpUrl httpUrl, List<Cookie> cookies) {
		String host = httpUrl.host();
		List<Cookie> list = cookieStore.get(host);
		if (list == null) {
			list = new ArrayList<>();
			cookieStore.put(host, list);
		} else {
			for (Cookie cookie : cookies) {
				Iterator<Cookie> it = list.iterator();
				while (it.hasNext()) {
					if (it.next().name().equals(cookie.name()))
						it.remove();
				}
			}
		}
		list.addAll(cookies);
	}

	/**
	 * 添加cookie
	 * 
	 * @param url     连接地址
	 * @param cookies 列表
	 */
	public void addCookie(String url, List<Cookie> cookies) {
		addCookie(HttpUrl.parse(url), cookies);
	}

	/**
	 * 添加cookie
	 * 
	 * @param httpUrl 连接地址
	 * @param cookies 列表
	 */
	public void addCookie(HttpUrl httpUrl, Cookie cookie) {
		String host = httpUrl.host();
		List<Cookie> cookies = cookieStore.get(host);
		if (cookies == null) {
			cookies = new ArrayList<>();
			cookieStore.put(host, cookies);
		} else {
			Iterator<Cookie> it = cookies.iterator();
			while (it.hasNext()) {
				if (it.next().name().equals(cookie.name()))
					it.remove();
			}
		}
		cookies.add(cookie);
	}

	/**
	 * 添加cookie
	 * 
	 * @param url     连接地址
	 * @param cookies 列表
	 */
	public void addCookie(String url, Cookie cookie) {
		addCookie(HttpUrl.parse(url), cookie);
	}

	/**
	 * 获取cookie信息
	 * 
	 * @return
	 */
	public Map<String, List<CookieInfo>> getCookieInfos() {
		Map<String, List<CookieInfo>> map = new HashMap<>();
		cookieStore.forEach((k, v) -> {
			List<CookieInfo> list = map.get(k);
			if (list == null) {
				list = new ArrayList<>();
				map.put(k, list);
			}
			for (Cookie cookie : v) {
				list.add(CookieInfo.parse(cookie));
			}
		});
		return map;
	}

	/**
	 * 初始化cookie
	 * 
	 * @param map
	 */
	public void initCookie(Map<String, List<CookieInfo>> cookieInfos) {
		if(cookieInfos == null)
			return;
		cookieInfos.forEach((k, v) -> {
			List<Cookie> list = cookieStore.get(k);
			if (list == null) {
				list = new ArrayList<>();
				cookieStore.put(k, list);
			}
			for (CookieInfo cookieInfo : v) {
				list.add(cookieInfo.toCookie());
			}
		});
	}

}
