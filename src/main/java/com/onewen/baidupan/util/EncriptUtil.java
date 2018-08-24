package com.onewen.baidupan.util;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

/**
 * 加密工具
 * 
 * @author 梁光运
 * @date 2018年8月21日
 */
public class EncriptUtil {

	/**
	 * 通过MD5数据加密
	 * 
	 * @param data
	 * @return
	 * @throws Exception 
	 */
	public static String encriptByMd5(String data) throws Exception {
		// 确定计算方法
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		// 加密后的字符串
		return bytesToHexString(md5.digest(data.getBytes("utf-8")));
	}

	/**
	 * 字节数组转换为十六进制字符串
	 * 
	 * @param bytes
	 * @return
	 */
	public static String bytesToHexString(byte[] bytes) {
		if (bytes == null) {
			return "null";
		}
		StringBuffer sb = new StringBuffer();

		for (int k = 0; k < bytes.length; k++) {
			if ((bytes[k] & 0xFF) < 16) {
				sb.append("0");
			}
			sb.append(Integer.toString(bytes[k] & 0xFF, 16));
		}
		return sb.toString();
	}

	/**
	 * 十六进制转字节数组
	 * 
	 * @param hexstring 十六进制字符串
	 * @return
	 */
	public static byte[] hexStringToBytes(String hexstring) {
		if ((hexstring == null) || (hexstring.length() % 2 != 0)) {
			return new byte[0];
		}

		byte[] dest = new byte[hexstring.length() / 2];

		for (int i = 0; i < dest.length; i++) {
			String val = hexstring.substring(2 * i, 2 * i + 2);
			dest[i] = (byte) Integer.parseInt(val, 16);
		}
		return dest;
	}

	/**
	 * 将base64编码后的公钥字符串转成PublicKey实例
	 * 
	 * @param publicKey base64编码后的公钥字符串
	 * @return PublicKey实例
	 * @throws Exception
	 */
	private static PublicKey getPublicKey(String publicKey) throws Exception {
		byte[] keyBytes = Base64.getDecoder().decode(publicKey);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePublic(keySpec);
	}

	/**
	 * 将base64编码后的私钥字符串转成PrivateKey实例
	 * 
	 * @param privateKey
	 * @return
	 * @throws Exception
	 */
	private static PrivateKey getPrivateKey(String privateKey) throws Exception {
		byte[] keyBytes = Base64.getDecoder().decode(privateKey);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePrivate(keySpec);
	}

	/**
	 * 公钥加密
	 * 
	 * @param content   待加密内容
	 * @param publicKey 公共密钥
	 * @return
	 * @throws Exception
	 */
	public static String encryptByPublicKey(String content, String publicKey) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKey));
		return Base64.getEncoder().encodeToString(cipher.doFinal(content.getBytes("utf-8")));
	}

	/**
	 * 私钥解密
	 * 
	 * @param content    待解密内容
	 * @param privateKey
	 * @return
	 * @throws Exception
	 */
	public static String decryptByPrivateKey(String content, String privateKey) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(privateKey));
		return new String(cipher.doFinal(Base64.getDecoder().decode(content.getBytes("utf-8"))));
	}

}
