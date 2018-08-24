package com.onewen.baidupan;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onewen.baidupan.model.Account;
import com.onewen.baidupan.model.PanFile;
import com.onewen.baidupan.service.BaiduPanService;
import com.onewen.baidupan.service.LoginService;

/**
 * 百度云盘
 * 
 * @author 梁光运
 * @date 2018年8月23日
 */
public class BaiduPan {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaiduPan.class);

	public static void main(String[] args) throws Exception {
		LoginService loginService = new LoginService();
		BaiduPanService baiduPanService = new BaiduPanService();
		Account account = loginService.startLogin("username", "password");
		if (account == null)
			return;
		List<PanFile> panFiles = baiduPanService.listFile(account, "/");
		for (PanFile panFile : panFiles) {
			LOGGER.info(panFile.getServer_filename());
		}
	}
}
