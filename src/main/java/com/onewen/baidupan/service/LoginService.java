package com.onewen.baidupan.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.onewen.baidupan.constant.Constant;
import com.onewen.baidupan.constant.ErrorCode;
import com.onewen.baidupan.model.Account;
import com.onewen.baidupan.repository.AccountRepository;
import com.onewen.baidupan.util.CookieStore;
import com.onewen.baidupan.util.EncriptUtil;
import com.onewen.baidupan.util.HttpUtil;

import okhttp3.Cookie;

/**
 * 登陆业务
 * 
 * @author 梁光运
 * @date 2018年8月21日
 */
public class LoginService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * 开始登陆
	 * 
	 * @param username 用户名
	 * @param password 密码
	 * @return
	 * @throws Exception
	 */
	public Account startLogin(String username, String password) throws Exception {
		Account account = AccountRepository.getInstance().getAccount();
		if (account == null || !username.equals(account.getUsername()))
			account = Account.build(Constant.getGid(), username, password);

		// 是否登陆
		if (isLogin(account)) {
			log.info("账号 [" + account.getNickname() + "] 已经登陆");
			return account;
		}

		// 初始化cookie
		account.getHttpUtil().getString(Constant.BAIDU_PAN_URL);
		Cookie cookie = account.getHttpUtil().getCookieStore().getCookie(Constant.BAIDU_PAN_URL, "BAIDUID");
		if (cookie != null)
			account.getHttpUtil().getCookieStore().addCookie(Constant.BAIDU_PASSPORT_URL, cookie);

		// 初始token
		initToken(account);

		// 初始化加密信息
		initRsaKey(account);

		// 获取登陆信息
		Map<String, Object> form = getLoginForm(account);

		// 登陆
		if (login(account, form))
			return account;
		return null;
	}

	/**
	 * 验证登陆
	 * 
	 * @param account    账号信息
	 * @param codeString 验证ID
	 * @param form       表单
	 * @throws Exception
	 */
	private boolean verifyLogin(Account account, String codeString, Map<String, Object> form) throws Exception {
		Scanner scan = new Scanner(System.in);
		String verifycode;
		while (true) {
			// 获取验证码
			downloadVerifyCodeImage(account, codeString);
			// 输入验证码
			log.info("请输入验证码:");
			verifycode = scan.nextLine();
			// 校验验证码
			String vsResult = account.getHttpUtil()
					.getString(Constant.getVerifyCodeUrl(account.getToken(), verifycode, codeString));
			vsResult = vsResult.substring(vsResult.indexOf("(") + 1, vsResult.lastIndexOf(")"));
			JSONObject jsonObject = JSONObject.parseObject(vsResult);
			jsonObject = jsonObject.getJSONObject("errInfo");
			if (jsonObject.getString("no") == null || jsonObject.getString("no").equals("0"))
				break;
			log.info("验证码: " + " [" + jsonObject.getString("no") + "] " + jsonObject.getString("msg"));
		}
		scan.close();

		// 添加表单信息
		form.put("codestring", codeString);
		form.put("vcodefrom", "checkuname");
		form.put("verifycode", verifycode);

		// 登陆
		return login(account, form);
	}

	/**
	 * 登陆
	 * 
	 * @param account 账号信息
	 * @param form    表单信息
	 * @throws Exception
	 */
	private boolean login(Account account, Map<String, Object> form) throws Exception {
		// 请求登陆
		String resp = account.getHttpUtil().post(Constant.BAIDU_PAN_LOGIN_URL, form);

		// 错误码判断
		resp = resp.substring(resp.indexOf("err_no="), resp.indexOf("\"+accounts"));
		Map<String, String> respParams = HttpUtil.getURLParams(resp);
		String errorCode = respParams.get("err_no");
		if (errorCode.equals("0")) {
			CookieStore cookieStore = account.getHttpUtil().getCookieStore();
			Cookie cookie = cookieStore.getCookie(Constant.BAIDU_PASSPORT_URL, "BDUSS");
			if (cookie != null)
				cookieStore.addCookie(Constant.BAIDU_PAN_HOME_URL, cookie);
			cookie = cookieStore.getCookie(Constant.BAIDU_PASSPORT_URL, "STOKEN");
			if (cookie != null)
				cookieStore.addCookie(Constant.BAIDU_PAN_HOME_URL, cookie);

			// 获取用户数据
			String json = account.getHttpUtil().getString(Constant.BAIDU_PAN_HOME_URL);
			json = json.substring(json.indexOf("context="), json.indexOf("require("));
			json = json.substring(json.indexOf("{"), json.lastIndexOf("};") + 1);
			JSONObject jsonObject = JSONObject.parseObject(json);
			account.setNickname(jsonObject.getString("username"));
			account.setUserPortrait(jsonObject.getString("photo"));
			account.setBdstoken(jsonObject.getString("bdstoken"));

			// 连接授权
			cookieStore.addCookie(Constant.PAN_API_SUPER_FILE, cookieStore.getCookie(Constant.BAIDU_PAN_HOME_URL));

			// 保存数据
			AccountRepository.getInstance().saveAccount(account);
			log.info("账号 [" + account.getNickname() + "] 登陆成功");
			return true;
		} else if (errorCode.equals("257")) {
			return verifyLogin(account, respParams.get("codeString"), form);
		} else {
			log.info("登陆失败, 错误码 [" + errorCode + "]," + ErrorCode.getLoginErrorMsg(Integer.valueOf(errorCode)));
			return false;
		}
	}

	/**
	 * 初始token
	 * 
	 * @param account 账号信息
	 * @throws IOException
	 */
	private void initToken(Account account) throws IOException {
		String jsonText = account.getHttpUtil().getString(Constant.getApiUrl(account.getGid()));
		jsonText = jsonText.substring(jsonText.indexOf("(") + 1, jsonText.lastIndexOf(")"));
		JSONObject jsonObject = JSONObject.parseObject(jsonText);
		String token = jsonObject.getJSONObject("data").getString("token");
		account.setToken(token);
	}

	/**
	 * 初始化加密信息
	 * 
	 * @param account 账号信息
	 * @throws Exception
	 */
	private void initRsaKey(Account account) throws Exception {
		String jsonText = account.getHttpUtil()
				.getString(Constant.getPublicKeyUrl(account.getToken(), account.getGid()));
		jsonText = jsonText.substring(jsonText.indexOf("(") + 1, jsonText.lastIndexOf(")"));
		JSONObject jsonObject = JSONObject.parseObject(jsonText);
		String publicKey = jsonObject.getString("pubkey");
		publicKey = publicKey.replace("-----BEGIN PUBLIC KEY-----", "");
		publicKey = publicKey.replace("-----END PUBLIC KEY-----", "");
		publicKey = publicKey.replace("\n", "");
		account.setPubkey(publicKey);
		account.setRsakey(jsonObject.getString("key"));
		account.setEncriptPass(EncriptUtil.encryptByPublicKey(account.getPassword(), publicKey));
	}

	/**
	 * 获取登陆信息
	 * 
	 * @param account 账号信息
	 * @return
	 */
	private Map<String, Object> getLoginForm(Account account) {
		Map<String, Object> forms = new HashMap<>();
		forms.put("staticpage", Constant.BAIDU_PAN_PASS_V3);
		forms.put("charset", "UTF-8");
		forms.put("token: ", account.getToken());
		forms.put("tpl", "netdisk");
		forms.put("subpro", "netdisk_web");
		forms.put("apiver", "v3");
		forms.put("tt", System.currentTimeMillis());
		forms.put("codestring", "");
		forms.put("safeflg", 0);
		forms.put("u", Constant.BAIDU_PAN_HOME_URL);
		forms.put("isPhone", false);
		forms.put("detect", 1);
		forms.put("gid", account.getGid());
		forms.put("quick_user", 0);
		forms.put("logintype", "basicLogin");
		forms.put("logLoginType", "pc_loginBasic");
		forms.put("idc", "");
		forms.put("loginmerge", true);
		forms.put("foreignusername", "");
		forms.put("username", account.getUsername());
		forms.put("password", account.getEncriptPass());
		forms.put("mem_pass", "no");
		forms.put("rsakey", account.getRsakey());
		forms.put("crypttype", 12);
		forms.put("ppui_logintime", 16413);
		forms.put("countrycode", "");
		forms.put("fp_uid", "");
		forms.put("fp_info", "");
		forms.put("loginversion", "v4");
		forms.put("dv",
				"tk0.44465256984785121534833657860@UUs0Q-6kvevmgIDIYsI34uEziDIzhRu4iRL54xL-"
						+ "FdLbDev9rTuWAQuxLevmgIDIYsI34uEziDIzhRu4iRL54xL-FdLbDev9rl694QuxGevmgIDIYsI34uEziDIzh"
						+ "Ru4iRL54xL-FdLbDev9rK6k4Qux0evmgIDIYsI34uEziDIzhRu4iRL54xL-FdLbDev9r~vxuQ6k3evmgIDIYs"
						+ "I34uEziDIzhRu4iRL54xL-FdLbDev9vTvkAQts0hU6kIT7kv~v1Tyu9I-74Ah93FnDIzREzh9I4QKEzixFpoC"
						+ "PEAQ6k2y7k2~7k3Tux2eRk2z6mTxuWrev9IYu9LeH046Azoh9HiRI4uDExARE-uzGbzSFsT_-jjPEjFYtwOBh"
						+ "FshvnTl7kG~IsoF5eT7WDKukGzvWIy692Kux2zv9rgu9vK6kvxuWI-6kGThsoPsAKLsvZ7liTGp~UGb4SMsIU"
						+ "GyiC7lgzBbAXMbXUMpD_ysdvmTT7kLKvnT-v9Gev9DKvmTYvkqev9DKvmTgukDT7k2yvq__");
		forms.put("traceid", 98101901);
		forms.put("callback", "parent." + Constant.getCallBackCode());
		return forms;
	}

	/**
	 * 下载验证码
	 * 
	 * @param codeString 验证码ID
	 * @throws IOException
	 */
	public void downloadVerifyCodeImage(Account account, String codeString) throws IOException {
		FileOutputStream fs = null;
		try {
			File file = new File("verifyCode.png");
			InputStream is = account.getHttpUtil().getResponse(Constant.getVerifyCodeImgUrl(codeString)).body()
					.byteStream();
			byte[] bs = new byte[1024];
			int n;
			fs = new FileOutputStream(file);
			while ((n = is.read(bs)) > 0) {
				fs.write(bs, 0, n);
			}
			log.info("验证码路径：" + file.getAbsolutePath());
		} finally {
			if (fs != null)
				fs.close();
		}

	}

	/**
	 * 是否已经登陆
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean isLogin(Account account) throws IOException {
		if (account == null)
			return false;
		String json = account.getHttpUtil().getString(Constant.PAN_API_LIST_FILE);
		JSONObject jsonObject = JSONObject.parseObject(json);
		return jsonObject.getInteger("errno") != null && jsonObject.getInteger("errno") == 0;
	}

}
