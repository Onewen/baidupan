package com.onewen.baidupan.task;

import java.util.concurrent.BlockingQueue;

import com.onewen.baidupan.model.Account;
import com.onewen.baidupan.service.BaiduPanService;

/**
 * 发送文件任务
 * 
 * @author 梁光运
 * @date 2018年9月4日
 */
public class SuperFileTask implements Runnable {

	private Account account;
	private String uploadid;
	private String serverPath;
	private byte[] bytes;
	private int partseq;
	private String md5;
	private BlockingQueue<SuperFileTask> queue;

	public SuperFileTask(Account account, String uploadid, String serverPath, byte[] bytes, int partseq,
			BlockingQueue<SuperFileTask> queue) {
		this.account = account;
		this.uploadid = uploadid;
		this.serverPath = serverPath;
		this.bytes = bytes;
		this.partseq = partseq;
		this.queue = queue;
	}

	public Account getAccount() {
		return account;
	}

	public String getUploadid() {
		return uploadid;
	}

	public String getServerPath() {
		return serverPath;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public int getPartseq() {
		return partseq;
	}

	public BlockingQueue<SuperFileTask> getQueue() {
		return queue;
	}

	public String getMd5() {
		return md5;
	}

	@Override
	public void run() {
		try {
			md5 = BaiduPanService.getInstance().superFile(account, uploadid, bytes, serverPath, partseq)
					.getString("md5");
		} finally {
			queue.offer(this);
		}
	}
}
