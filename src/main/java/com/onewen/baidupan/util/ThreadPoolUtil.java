package com.onewen.baidupan.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池服务
 * 
 * @author 梁光运
 * @date 2018年9月3日
 */
public class ThreadPoolUtil {

	private final static ExecutorService uploadFilePool = Executors.newFixedThreadPool(4,
			new NameThreadFactory("uploadfile"));

	public static ExecutorService getUploadFilePool() {
		return uploadFilePool;
	}

}
