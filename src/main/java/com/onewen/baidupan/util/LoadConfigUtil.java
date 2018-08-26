package com.onewen.baidupan.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

/**
 * 加载配置工具
 * 
 * @author 梁光运
 * @date 2018年8月26日
 */
public class LoadConfigUtil {

	private final static Logger LOGGER = LoggerFactory.getLogger(LoadConfigUtil.class);

	/**
	 * 加载配置
	 * 
	 * @param fileDir
	 *            文件目录
	 * @param packageDir
	 *            包目录
	 * @throws Exception
	 *             加载失败
	 */
	public static void loadConfig(String fileDir, String packageDir) throws Exception {
		File file = new File(LoadConfigUtil.class.getClass().getResource("/").getPath() +"/"+ fileDir);
		if (!file.isDirectory())
			throw new FileNotFoundException();
		for (File f : file.listFiles()) {
			try {
				if (!f.isFile())
					continue;
				String name = f.getName();
				if (!name.endsWith(".json"))
					continue;
				Class<?> clazz = Class.forName(packageDir + "." + name.substring(0, name.lastIndexOf('.')));
				loadConfig(f, clazz);
			} catch (Exception e) {
				LOGGER.error("load config [" + f.getName() + "]");
				throw e;
			}
		}
	}

	/**
	 * 加载配置
	 * 
	 * @param file
	 *            文件
	 * @param clazz
	 *            类
	 * @throws Exception
	 *             加载失败
	 */
	public static void loadConfig(File file, Class<?> clazz) throws Exception {
		BufferedReader br = null;
		try {
 			br = new BufferedReader(new FileReader(file));
			String str = null;
			StringBuffer sb = new StringBuffer();
			while( (str = br.readLine()) != null)
				sb.append(str);
			List<?> list = JSON.parseArray(sb.toString(), clazz);
			for (Method method : clazz.getDeclaredMethods()) {
				method.setAccessible(true);
				if (method.getAnnotation(LoadConfig.class) == null)
					continue;
				if (!Modifier.isStatic(method.getModifiers()))
					throw new Exception("初始化配置方法 [" + method.getName() + "] 不是静态方法");
				method.invoke(null, list);
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}

	}

	/**
	 * 
	 * @param fileName
	 *            文件名
	 * @param clazz
	 *            类
	 * @throws Exception
	 *             加载失败
	 */
	public static void loadConfig(String fileName, Class<?> clazz) throws Exception {
		File file = new File(LoadConfigUtil.class.getClass().getResource("/").getPath() + "/"+ fileName);
		if (!file.isFile())
			throw new Exception(fileName + "不是一个文件");
		loadConfig(file, clazz);
	}
}
