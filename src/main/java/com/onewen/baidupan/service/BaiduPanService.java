package com.onewen.baidupan.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.onewen.baidupan.constant.Constant;
import com.onewen.baidupan.model.Account;
import com.onewen.baidupan.model.PanFile;
import com.onewen.baidupan.util.EncriptUtil;

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
			String url = Constant.PAN_API_LIST_FILE + "?order=time&desc=1" + "&dir=" + dir + "&bdstoken="
					+ account.getBdstoken();
			String jsonText = account.getHttpUtil().getString(url);
			JSONObject jsonObject = JSONObject.parseObject(jsonText);
			if (jsonObject.getInteger("errno") != 0) {
				log.info("获取文件列表失败,错误码 [" + jsonObject.getInteger("errno") + "]");
				return null;
			}
			return jsonObject.getJSONArray("list").toJavaList(PanFile.class);
		} catch (Exception e) {
			log.error("获取 [" + dir + "] 文件列表失败:", e);
			return null;
		}
	}

	/**
	 * 新建文件夹
	 * 
	 * @param account 账号信息
	 * @param path    路径
	 * @return
	 */
	public boolean newFolder(Account account, String path) {
		try {
			Map<String, Object> form = new HashMap<>();
			form.put("path", path);
			form.put("isdir", 1);
			form.put("block_list", "[]");
			String url = Constant.PAN_API_CREATE_FILE
					+ "a=commit&channel=chunlei&web=1&app_id=250528&clienttype=0&bdstoken=" + account.getBdstoken();
			String json = account.getHttpUtil().post(url, form);
			JSONObject jsonObject = JSONObject.parseObject(json);
			if (jsonObject.getInteger("errno") == 0)
				return true;
			log.info("新建文件夹 [" + path + "] 失败,错误码 [" + jsonObject.getInteger("errno") + "]");
			return false;
		} catch (Exception e) {
			log.error("新建文件夹 [" + path + "] 失败", e);
			return false;
		}
	}

	/**
	 * 批量删除文件
	 * 
	 * @param account 账号信息
	 * @param path    路径列表
	 */
	public boolean deleteFile(Account account, List<String> paths) {
		try {
			Map<String, Object> form = new HashMap<>();
			form.put("filelist", JSON.toJSONString(paths));
			String url = Constant.PAN_API_FILE_MANAGER
					+ "?opera=delete&async=2&onnest=fail&channel=chunlei&web=1&app_id=250528&clienttype=0&bdstoken="
					+ account.getBdstoken();
			String json = account.getHttpUtil().post(url, form);
			JSONObject jsonObject = JSONObject.parseObject(json);
			if (jsonObject.getInteger("errno") == 0)
				return true;
			log.info("删除文件 " + paths + " 失败,错误码 [" + jsonObject.getInteger("errno") + "]");
			return false;
		} catch (Exception e) {
			log.error("删除文件 " + paths + " 失败", e);
			return false;
		}
	}

	/**
	 * 删除单个文件
	 * 
	 * @param account 账号信息
	 * @param path    路径
	 */
	public boolean deleteFile(Account account, String path) {
		List<String> list = new ArrayList<>();
		list.add(path);
		return deleteFile(account, list);
	}

	/**
	 * 上传文件
	 * 
	 * @param account    账号信息
	 * @param filePath   本地路径
	 * @param serverPath 服务器路径
	 */
	public void uplaodFile(Account account, String filePath, String serverPath) {
		File file = new File(filePath);
		if (!file.exists()) {
			log.error("找不到 [" + filePath + "] 文件");
			return;
		}

		if (!file.isFile()) {
			log.error("[" + filePath + "] 不是一个文件");
			return;
		}

		// 预上传文件
		serverPath = serverPath.endsWith("/") ? serverPath + file.getName() : serverPath + "/" + file.getName();
		int loaclTime = (int) (System.currentTimeMillis() / 1000);
		JSONObject precreateResult = precreateFile(account, file, serverPath, loaclTime);
		if (precreateResult == null || precreateResult.getInteger("errno") != 0) {
			log.info("预上传 [" + filePath + "] 文件");
			return;
		}

		// 上传文件
		String uploadid = precreateResult.getString("uploadid");
		FileInputStream fs = null;
		try {
			JSONObject rapidResult = rapidUploadFile(account, file, serverPath, loaclTime);
			if (rapidResult == null || rapidResult.getInteger("errno") != 0) {
				fs = new FileInputStream(file);
				List<String> blockList = new ArrayList<>();
				int len = (int) Math.min(file.length(), Constant.FILE_CHUNK_SIZE);
				byte[] bytes = new byte[len];
				int n = 0;
				int off = 0;
				int readAllSize = 0;
				int partseq = 0;
				while ((n = fs.read(bytes, off, len)) > 0) {
					len -= n;
					off += n;
					readAllSize += n;
					if (len > 0)
						continue;
					JSONObject superResult = superFile(account, uploadid, bytes, serverPath, partseq);
					blockList.add(superResult.getString("md5"));
					len = (int) Math.min(file.length() - readAllSize, Constant.FILE_CHUNK_SIZE);
					off = 0;
					++partseq;
				}
				// 上传完成
				JSONObject uploadResult = uploadFileFinish(account, file, uploadid, serverPath, blockList, loaclTime);
				System.out.println(uploadResult);
			} else {
				System.out.println(rapidResult);
			}
		} catch (Exception e) {
			log.error("上传 [" + filePath + "] 文件失败", e);
		} finally {
			if (fs != null) {
				try {
					fs.close();
				} catch (IOException e) {
					log.error("关闭文件失败", e);
				}
			}
		}
	}

	/**
	 * 预创建文件
	 * 
	 * @param account    账号信息
	 * @param fileSize   文件大小
	 * @param serverPath 服务器路径
	 * @param loaclTime  本地时间
	 * @return
	 */
	private JSONObject precreateFile(Account account, File file, String serverPath, int loaclTime) {
		try {
			Map<String, Object> form = new HashMap<>();
			form.put("path", serverPath);
			form.put("autoinit", 1);
			form.put("local_mtime", loaclTime);
			String blockList = file.length() > Constant.FILE_CHUNK_SIZE
					? "[\"5910a591dd8fc18c32a8f3df4fdc1761\",\"a5fc157d78e6ad1c7e114b056c92821e\"]"
					: "[\"5910a591dd8fc18c32a8f3df4fdc1761\"]";
			form.put("block_list", blockList);
			String url = Constant.PAN_API_PRECREATE_FILE + "?channel=chunlei&web=1&app_id=250528&clienttype=0"
					+ "&bdstoken=" + account.getBdstoken();
			String json = account.getHttpUtil().post(url, form);
			return JSONObject.parseObject(json);
		} catch (IOException e) {
			log.error("预创建文件错误", e);
			return null;
		}
	}

	/**
	 * 秒传
	 * 
	 * @param account    账号信息
	 * @param file       文件
	 * @param serverPath 服务路径
	 * @param loaclTime  本地时间
	 * @return
	 */
	private JSONObject rapidUploadFile(Account account, File file, String serverPath, int loaclTime) {
		if (file.length() < Constant.SLICE_FIRST_SIZE)
			return null;
		BufferedInputStream bs = null;
		try {
			// 计算头部MD5
			bs = new BufferedInputStream(new FileInputStream(file));
			bs.mark(Constant.SLICE_FIRST_SIZE);
			byte[] bytes = new byte[Constant.SLICE_FIRST_SIZE];
			int n = 0, off = 0, len = Constant.SLICE_FIRST_SIZE;
			while ((n = bs.read(bytes, off, len)) > -1) {
				len -= n;
				off += n;
				if (len <= 0)
					break;
			}
			String sliceMd5 = EncriptUtil.encriptByMd5(bytes);
			bs.reset();
			String contentMd5 = EncriptUtil.encriptByMd5(bs);
			// 表单
			Map<String, Object> form = new HashMap<>();
			form.put("path", serverPath);
			form.put("content-length", file.length());
			form.put("content-md5", contentMd5);
			form.put("slice-md5", sliceMd5);
			form.put("local_mtime", loaclTime);
			// 连接地址
			String url = Constant.PAN_API_RAPID_UPLOAD_FILE
					+ "?rtype=1&channel=chunlei&web=1&app_id=250528&clienttype=0" + "&bdstoken="
					+ account.getBdstoken();
			String json = account.getHttpUtil().post(url, form);
			return JSONObject.parseObject(json);
		} catch (Exception e) {
			log.error("", e);
			return null;
		} finally {
			if (bs != null) {
				try {
					bs.close();
				} catch (IOException e) {
					log.error("关闭文件 [" + file.getName() + "] 流失败", e);
				}
			}
		}
	}

	/**
	 * 发送文件
	 * 
	 * @param account    账号信息
	 * @param uploadid   上传ID
	 * @param bytes      上传数据
	 * @param serverPath 服务器路径
	 * @param partseq    分片号
	 * @return
	 */
	private JSONObject superFile(Account account, String uploadid, byte[] bytes, String serverPath, int partseq) {
		try {
			String url = Constant.PAN_API_SUPER_FILE + "?method=upload&app_id=250528&channel=chunlei&clienttype=0&web=1"
					+ "&BDUSS="
					+ account.getHttpUtil().getCookieStore().getCookie(Constant.BAIDU_PAN_HOME_URL, "BDUSS").value()
					+ "&path=" + serverPath + "&uploadid=" + uploadid + "&uploadsign=0&partseq=" + partseq;
			account.getHttpUtil().options(url);
			String json = account.getHttpUtil().post(url, bytes);
			return JSONObject.parseObject(json);
		} catch (IOException e) {
			log.error("发送 [" + serverPath + "] 文件失败", e);
			return null;
		}

	}

	/**
	 * 上传文件完成
	 * 
	 * @param account    账号信息
	 * @param file       文件
	 * @param uploadid   上传ID
	 * @param serverPath 服务器路径
	 * @param blockList  块列表
	 * @param loaclTime  本地时间戳
	 * @return
	 */
	private JSONObject uploadFileFinish(Account account, File file, String uploadid, String serverPath,
			List<String> blockList, int loaclTime) {
		try {
			Map<String, Object> form = new HashMap<>();
			form.put("path", serverPath);
			form.put("uploadid", uploadid);
			form.put("size", file.length());
			form.put("block_list", JSON.toJSONString(blockList));
			form.put("local_mtime", loaclTime);
			String url = Constant.PAN_API_CREATE_FILE
					+ "isdir=0&rtype=1&channel=chunlei&web=1&app_id=250528&clienttype=0&bdstoken="
					+ account.getBdstoken();
			String json = account.getHttpUtil().post(url, form);
			return JSONObject.parseObject(json);
		} catch (Exception e) {
			log.error("上传文件[" + file.getName() + "]失败", e);
			return null;
		}

	}

}
