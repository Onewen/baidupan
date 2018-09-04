package com.onewen.baidupan.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.onewen.baidupan.constant.Constant;
import com.onewen.baidupan.model.Account;
import com.onewen.baidupan.model.PanFile;
import com.onewen.baidupan.repository.AccountRepository;
import com.onewen.baidupan.task.SuperFileTask;
import com.onewen.baidupan.task.UploadFileTask;
import com.onewen.baidupan.util.CookieStore;
import com.onewen.baidupan.util.EncriptUtil;

/**
 * 云盘业务
 * 
 * @author 梁光运
 * @date 2018年8月22日
 */
public class BaiduPanService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private static BaiduPanService instance;

	public static BaiduPanService getInstance() {
		if (instance == null)
			instance = new BaiduPanService();
		return instance;
	}

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
	 * 上传文件或者目录
	 * 
	 * @param account    账号信息
	 * @param filePath   本地路径
	 * @param serverPath 服务器路径
	 */
	public void uplaodFileOrDir(Account account, String path, String serverPath) {
		File file = new File(path);
		if (!file.exists()) {
			log.error("找不到 [" + file.getName() + "] 文件或者目录");
			return;
		}
		uplaodFileOrDir(account, file, serverPath);
	}

	/**
	 * 上传文件或者目录
	 * 
	 * @param account    账号信息
	 * @param filePath   本地路径
	 * @param serverPath 服务器路径
	 */
	public void uplaodFileOrDir(Account account, File file, String serverPath) {
		if (file.isFile()) {
			UploadFileTask task = new UploadFileTask(account, file, serverPath);
			ThreadPoolService.getInstance().getUploadFilePool().execute(task);
		} else if (file.isDirectory()) {
			serverPath = serverPath.endsWith("/") ? serverPath + file.getName() : serverPath + "/" + file.getName();
			for (File f : file.listFiles()) {
				uplaodFileOrDir(account, f, serverPath);
			}
		}

	}

	/**
	 * 上传文件
	 * 
	 * @param account    账号信息
	 * @param file       待上传文件
	 * @param serverPath 服务器路径
	 */
	public void uplaodFile(Account account, File file, String serverPath) {
		if (!file.exists()) {
			log.error("找不到 [" + file.getName() + "] 文件");
			return;
		}

		if (!file.isFile()) {
			log.error("[" + file.getName() + "] 不是一个文件");
			return;
		}

		// 预上传文件
		serverPath = serverPath.endsWith("/") ? serverPath + file.getName() : serverPath + "/" + file.getName();
		int loaclTime = (int) (System.currentTimeMillis() / 1000);
		JSONObject precreateResult = precreateFile(account, file, serverPath, loaclTime);
		if (precreateResult == null || precreateResult.getInteger("errno") != 0) {
			log.info("预上传 [" + file.getName() + "] 文件");
			return;
		}

		// 上传文件
		String uploadid = precreateResult.getString("uploadid");
		JSONObject rapidResult = rapidUploadFile(account, file, serverPath, loaclTime);
		if (rapidResult == null || rapidResult.getInteger("errno") != 0) {
			String blockList;
			if (file.length() > Constant.FILE_CHUNK_SIZE)
				blockList = multiThreadSuperFile(account, uploadid, file, serverPath);
			else
				blockList = signalThreadSuperFile(account, uploadid, file, serverPath);
			// 上传完成
			JSONObject uploadResult = uploadFileFinish(account, file, uploadid, serverPath, blockList, loaclTime);
			System.out.println(uploadResult);
		} else {
			System.out.println(rapidResult);
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
	 * 多线程发送文件
	 * 
	 * @param account    账号信息
	 * @param uploadid   上传ID
	 * @param file       文件
	 * @param serverPath 服务器路径
	 * @return
	 */
	private String multiThreadSuperFile(Account account, String uploadid, File file, String serverPath) {
		FileInputStream fs = null;
		try {
			BlockingQueue<SuperFileTask> queue = new LinkedBlockingQueue<>();
			fs = new FileInputStream(file);
			int len = (int) Math.min(file.length(), Constant.FILE_CHUNK_SIZE);
			byte[] bytes = new byte[len];
			int n = 0;
			int off = 0;
			int readAllSize = 0;
			int partseq = 0;
			while (len > 0 && (n = fs.read(bytes, off, len)) > 0) {
				len -= n;
				off += n;
				readAllSize += n;
				if (len > 0)
					continue;
				SuperFileTask task = new SuperFileTask(account, uploadid, serverPath, bytes, partseq, queue);
				ThreadPoolService.getInstance().getSuperFilePool().execute(task);
				len = (int) Math.min(file.length() - readAllSize, Constant.FILE_CHUNK_SIZE);
				bytes = new byte[len];
				off = 0;
				++partseq;
			}
			String[] blockList = new String[partseq];
			for (int i = partseq; i > 0; i--) {
				SuperFileTask task = queue.take();
				blockList[task.getPartseq()] = task.getMd5();
			}
			return JSON.toJSONString(blockList);
		} catch (Exception e) {
			log.error("上传 [" + file.getName() + "] 文件失败", e);
			return null;
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
	 * 单线程发送文件
	 * 
	 * @param account    账号信息
	 * @param uploadid   上传ID
	 * @param file       文件
	 * @param serverPath 服务器路径
	 * @return
	 */
	private String signalThreadSuperFile(Account account, String uploadid, File file, String serverPath) {
		FileInputStream fs = null;
		try {
			List<String> blockList = new ArrayList<>();
			fs = new FileInputStream(file);
			int len = (int) Math.min(file.length(), Constant.FILE_CHUNK_SIZE);
			byte[] bytes = new byte[len];
			int n = 0;
			int off = 0;
			int readAllSize = 0;
			int partseq = 0;
			while (len > 0 && (n = fs.read(bytes, off, len)) > -1) {
				len -= n;
				off += n;
				readAllSize += n;
				if (len > 0)
					continue;
				JSONObject superRsullt = superFile(account, uploadid, bytes, serverPath, partseq);
				blockList.add(superRsullt.getString("md5"));
				len = (int) Math.min(file.length() - readAllSize, Constant.FILE_CHUNK_SIZE);
				bytes = new byte[len];
				off = 0;
				++partseq;
			}
			return JSON.toJSONString(blockList);
		} catch (Exception e) {
			log.error("上传 [" + file.getName() + "] 文件失败", e);
			return null;
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
	 * 发送文件
	 * 
	 * @param account    账号信息
	 * @param uploadid   上传ID
	 * @param bytes      上传数据
	 * @param serverPath 服务器路径
	 * @param partseq    分片号
	 * @return
	 */
	public JSONObject superFile(Account account, String uploadid, byte[] bytes, String serverPath, int partseq) {
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
			String blockList, int loaclTime) {
		try {
			Map<String, Object> form = new HashMap<>();
			form.put("path", serverPath);
			form.put("uploadid", uploadid);
			form.put("size", file.length());
			form.put("block_list", blockList);
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

	/**
	 * 下载文件
	 * 
	 * @param account  账号信息
	 * @param panFile  文件
	 * @param savePath 保存路径
	 */
	public void downloadFile(Account account, PanFile panFile, String savePath) {
		if (panFile.isIsdir())
			return;
		BufferedOutputStream bs = null;
		try {
			JSONObject json = null;
			for (int i = 0; i < 2; i++) {
				if (account.getSign1() != null && account.getSign3() != null) {
					// 计算盐值
					String sign = EncriptUtil.sign(account.getSign3(), account.getSign1());

					// 组装链接
					String url = Constant.PAN_API_DOWNLOAD_FILE
							+ "?type=dlink&channel=chunlei&web=1&app_id=250528&clienttype=0" + "&bdstoken="
							+ account.getBdstoken() + "&sign=" + sign + "&timestamp=" + account.getTimestamp()
							+ "&fidlist=" + "[" + panFile.getFs_id() + "]";

					// 请求下载文件
					json = JSONObject.parseObject(account.getHttpUtil().getString(url));
					if (json.getIntValue("errno") == 0)
						break;
				}
				LoginService.getInstance().loadUserInfo(account);
				AccountRepository.getInstance().saveAccount(account);
			}
			if (json.getIntValue("errno") != 0) {
				log.info("下载文件失败, 错误码:" + json.getIntValue("errno"));
				return;
			}

			// 授权
			CookieStore cookieStore = account.getHttpUtil().getCookieStore();
			String dlink = json.getJSONArray("dlink").getJSONObject(0).getString("dlink");
			cookieStore.addCookie(dlink, cookieStore.getCookie(Constant.BAIDU_PAN_HOME_URL));

			// 下载文件
			InputStream is = account.getHttpUtil().getResponse(dlink).body().byteStream();
			byte[] bytes = new byte[10240];
			int n;
			File file = new File(savePath + panFile.getPath());
			file.getParentFile().mkdirs();
			bs = new BufferedOutputStream(new FileOutputStream(file));
			while ((n = is.read(bytes)) > 0) {
				bs.write(bytes, 0, n);
			}
			bs.flush();
			log.info("下载完成:" + file.getAbsolutePath());
		} catch (IOException e) {
			log.error("下载文件失败", e);
		} finally {
			if (bs != null)
				try {
					bs.close();
				} catch (IOException e) {
					log.error("关闭下载 [" + savePath + "] 文件失败");
				}
		}
	}

}
