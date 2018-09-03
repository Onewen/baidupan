package com.onewen.baidupan.task;

import java.io.File;

import com.onewen.baidupan.model.Account;
import com.onewen.baidupan.service.BaiduPanService;

/**
 * 上传文件任务
 * 
 * @author 梁光运
 * @date 2018年9月3日
 */
public class UploadFileTask implements Runnable {

	// 账号信息
	private final Account account;

	// 待上传文件
	private final File file;

	// 上传服务器路径
	private final String serverPath;

	public UploadFileTask(Account account, File file, String serverPath) {
		this.account = account;
		this.file = file;
		this.serverPath = serverPath;
	}

	@Override
	public void run() {
		BaiduPanService.getInstance().uplaodFile(account, file, serverPath);
	}

}
