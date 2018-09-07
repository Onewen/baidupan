package com.onewen.baidupan.task;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onewen.baidupan.model.Account;

/**
 * 下载包任务
 * 
 * @author 梁光运
 * @date 2018年9月7日
 */
public class DownLoadPackTask implements Runnable {

	private final static Logger log = LoggerFactory.getLogger(DownLoadPackTask.class);

	// 账号信息
	private Account account;

	// 下载链接
	private final String dlink;

	// 开始位置
	private final long startPos;

	// 下载大小
	private final int size;

	// 下载数据
	private byte[] bytes;

	// 下载是否完成
	private boolean finish;

	public DownLoadPackTask(Account account, String dlink, long startPos, int size) {
		this.dlink = dlink;
		this.startPos = startPos;
		this.size = size;
		this.finish = false;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public boolean isFinish() {
		return finish;
	}

	@Override
	public void run() {
		InputStream is = null;
		try {
			is = account.getHttpUtil().getResponse(dlink).body().byteStream();
			long skipSize = is.skip(startPos);
			if (skipSize != startPos) {
				log.error("download skip [" + skipSize + "] error");
				return;
			}
			int len = size;
			int off = 0, n = 0;
			bytes = new byte[len];
			while ((n = is.read(bytes, off, len)) > -1) {
				len -= n;
				off += n;
				if (len <= 0)
					break;
			}
			finish = true;
		} catch (Exception e) {
			log.error("downLoad pack task error", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					log.error("download close stream error", e);
				}
			}
		}
	}

}
