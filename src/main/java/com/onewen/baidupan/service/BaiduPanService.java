package com.onewen.baidupan.service;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.onewen.baidupan.constant.Constant;
import com.onewen.baidupan.model.Account;
import com.onewen.baidupan.model.PanFile;

/**
 * 云盘业务
 * 
 * @author 梁光运
 * @date 2018年8月22日
 */
public class BaiduPanService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * 获取文件列表
	 * 
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	public List<PanFile> listFile(Account account, String dir) {
		try {
			String jsonText = account.getHttpUtil().getString(Constant.getListFileUrl(dir));
			JSONObject jsonObject = JSONObject.parseObject(jsonText);
			if (jsonObject.getInteger("errno") != 0) {
				log.info("获取文件列表失败,错误码 [" + jsonObject.getInteger("errno") + "]");
				return null;
			}
			return jsonObject.getJSONArray("list").toJavaList(PanFile.class);
		} catch (Exception e) {
			log.error("获取文件列表失败:", e);
			return null;
		}
	}

}
