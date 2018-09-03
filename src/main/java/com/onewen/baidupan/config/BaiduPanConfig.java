package com.onewen.baidupan.config;

import java.util.List;

import com.onewen.baidupan.util.LoadConfig;

/**
 * 配置信息
 * 
 * @author 梁光运
 * @date 2018年9月3日
 */
public class BaiduPanConfig {

	private static BaiduPanConfig config;

	// 下载文件目录
	private String downloadDir;
	
	// 上传线程数量
	private int uploadFileThreads;

	public String getDownloadDir() {
		return downloadDir;
	}

	public void setDownloadDir(String downloadDir) {
		this.downloadDir = downloadDir;
	}
	
	public int getUploadFileThreads() {
		return uploadFileThreads;
	}

	public void setUploadFileThreads(int uploadFileThreads) {
		this.uploadFileThreads = uploadFileThreads;
	}

	public static BaiduPanConfig getConfig() {
		return config;
	}

	@LoadConfig
	private static void init(List<BaiduPanConfig> configs) {
		if (configs.isEmpty())
			return;
		config = configs.get(0);
	}

}
