package com.onewen.baidupan.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.onewen.baidupan.model.Account;

/**
 * 账号仓储
 * 
 * @author 梁光运
 * @date 2018年8月24日
 */
public class AccountRepository {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	// 保存文件名
	private final static String FILE_NAME = "account";

	// 账号信息
	private Map<String, Account> accounts = new HashMap<>();

	private static AccountRepository instance;

	public static AccountRepository getInstance() {
		if (instance == null)
			instance = new AccountRepository();
		return instance;
	}

	public static void setInstance(AccountRepository instance) {
		AccountRepository.instance = instance;
	}

	/**
	 * 获取账号信息
	 * 
	 * @return
	 */
	public Account getAccount(String username) {
		Account account = accounts.get(username);
		if (account != null)
			return account;
		File file = new File(FILE_NAME + "-" + username + ".json");
		if (!file.exists() || !file.isFile())
			return null;
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(file);
			account = JSON.parseObject(fs, Account.class);
			account.getHttpUtil().getCookieStore().initCookie(account.getCookieInfos());
			accounts.put(username, account);
			return account;
		} catch (Exception e) {
			log.error("加载账号信息失败", e);
			return null;
		} finally {
			if (fs != null) {
				try {
					fs.close();
				} catch (IOException e) {
					log.error("关闭文件", e);
				}
			}
		}
	}

	/**
	 * 保存账号信息
	 * 
	 * @param account 账号信息
	 */
	public void saveAccount(Account account) {
		FileWriter fw = null;
		try {
			account.setCookieInfos(account.getHttpUtil().getCookieStore().getCookieInfos());
			String json = JSON.toJSONString(account);
			fw = new FileWriter(FILE_NAME + "-" + account.getUsername() + ".json");
			fw.write(json);
		} catch (Exception e) {
			log.error("保存账号信息失败，", e);
		} finally {
			if (fw != null)
				try {
					fw.close();
				} catch (IOException e) {
					log.error("关闭文件", e);
				}
		}

	}

}
