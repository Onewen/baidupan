package com.onewen.baidupan.model;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;
import com.onewen.baidupan.util.CookieStore.CookieInfo;
import com.onewen.baidupan.util.HttpUtil;

/**
 * 账号信息
 * 
 * @author 梁光运
 * @date 2018年8月21日
 */
public class Account {

	// gid
	private String gid;

	// token
	private String token;

	// 公共密钥
	private String pubkey;

	// 公共密钥主键
	private String rsakey;

	// 用户名
	private String username;

	// 密码
	private String password;

	// 加密密码
	private String encriptPass;

	// cookie数据
	private Map<String, List<CookieInfo>> cookieInfos;

	// 头像ID
	private String userPortrait;

	// 昵称
	private String nickname;

	// bdstoken
	private String bdstoken;

	// sign1
	private String sign1;

	// sign3
	private String sign3;

	// 时间戳
	private int timestamp;

	// 访问HTTP工具
	@JSONField(serialize = false)
	private final HttpUtil httpUtil = new HttpUtil();

	public static Account build(String gid, String username, String password) {
		Account account = new Account();
		account.gid = gid;
		account.username = username;
		account.password = password;
		return account;
	}

	public String getGid() {
		return gid;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getPubkey() {
		return pubkey;
	}

	public void setPubkey(String pubkey) {
		this.pubkey = pubkey;
	}

	public String getRsakey() {
		return rsakey;
	}

	public void setRsakey(String rsakey) {
		this.rsakey = rsakey;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEncriptPass() {
		return encriptPass;
	}

	public void setEncriptPass(String encriptPass) {
		this.encriptPass = encriptPass;
	}

	public Map<String, List<CookieInfo>> getCookieInfos() {
		return cookieInfos;
	}

	public void setCookieInfos(Map<String, List<CookieInfo>> cookieInfos) {
		this.cookieInfos = cookieInfos;
	}

	public HttpUtil getHttpUtil() {
		return httpUtil;
	}

	public String getUserPortrait() {
		return userPortrait;
	}

	public void setUserPortrait(String userPortrait) {
		this.userPortrait = userPortrait;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getBdstoken() {
		return bdstoken;
	}

	public void setBdstoken(String bdstoken) {
		this.bdstoken = bdstoken;
	}

	public String getSign1() {
		return sign1;
	}

	public void setSign1(String sign1) {
		this.sign1 = sign1;
	}

	public String getSign3() {
		return sign3;
	}

	public void setSign3(String sign3) {
		this.sign3 = sign3;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

}
