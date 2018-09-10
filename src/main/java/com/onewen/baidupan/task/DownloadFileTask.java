package com.onewen.baidupan.task;

import com.onewen.baidupan.model.Account;
import com.onewen.baidupan.model.PanFile;
import com.onewen.baidupan.service.BaiduPanService;

/**
 * 下载任务
 * 
 * @author 梁光运
 * @date 2018年9月10日
 */
public class DownloadFileTask implements Runnable {

	private final Account account;
	private final PanFile panFile;
	private final String savePath;

	public DownloadFileTask(Account account, PanFile panFile, String savePath) {
		this.account = account;
		this.panFile = panFile;
		this.savePath = savePath;
	}

	@Override
	public void run() {
		BaiduPanService.getInstance().downloadFile(account, panFile, savePath);
	}
}
