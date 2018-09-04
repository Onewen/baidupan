package com.onewen.baidupan.service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.onewen.baidupan.config.BaiduPanConfig;
import com.onewen.baidupan.util.NameThreadFactory;

/**
 * 线程池服务
 * 
 * @author 梁光运
 * @date 2018年9月3日
 */
public class ThreadPoolService {

	private static ThreadPoolService instance;

	public static ThreadPoolService getInstance() {
		if (instance == null)
			instance = new ThreadPoolService();
		return instance;
	}

	private final ExecutorService uploadFilePool = Executors
			.newFixedThreadPool(BaiduPanConfig.getConfig().getUploadFileThreads(), new NameThreadFactory("uploadfile"));

	private final ExecutorService superFilePool = new ThreadPoolExecutor(
			BaiduPanConfig.getConfig().getSuperFileThreads(), BaiduPanConfig.getConfig().getSuperFileThreads(), 0L,
			TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(BaiduPanConfig.getConfig().getSuperFileQueue()),
			new NameThreadFactory("superfile"), new RejectedExecutionHandler() {
				@Override
				public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
					try {
						executor.getQueue().put(r);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});

	public ExecutorService getUploadFilePool() {
		return uploadFilePool;
	}

	public ExecutorService getSuperFilePool() {
		return superFilePool;
	}

	public void showndown() {
		superFilePool.shutdown();
		uploadFilePool.shutdown();
	}

}
