package com.onewen.baidupan;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onewen.baidupan.model.Account;
import com.onewen.baidupan.model.PanFile;
import com.onewen.baidupan.service.BaiduPanService;
import com.onewen.baidupan.service.LoginService;
import com.onewen.baidupan.util.LoadConfigUtil;

/**
 * 百度云盘
 * 
 * @author 梁光运
 * @date 2018年8月23日
 */
public class BaiduPan {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaiduPan.class);

	public static void main(String[] args) throws Exception {
		// 加载配置文件
		LoadConfigUtil.loadConfig("config", "com.onewen.baidupan.config");

		// 登陆
		Account account = LoginService.getInstance().startLogin("username", "password");
		if (account == null)
			return;
		List<PanFile> panFiles = BaiduPanService.getInstance().listFile(account, "/");
		for (PanFile panFile : panFiles) {
			if(!panFile.isIsdir()) {
				BaiduPanService.getInstance().downloadFile(account, panFile, "F:\\BaiduYunDownload");
				LOGGER.info(panFile.getServer_filename());
				break;
			}
		}
	}
}
