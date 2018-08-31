package com.onewen.baidupan.util;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

import org.apache.commons.codec.digest.DigestUtils;

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
		return DigestUtils.md5Hex(data);
	}

	/**
	 * 通过MD5数据加密
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static String encriptByMd5(InputStream is) throws Exception {
		return DigestUtils.md5Hex(is);
	}

	/**
	 * 通过MD5数据加密
	 * 
	 * @param bytes
	 * @return
	 * @throws Exception
	 */
	public static String encriptByMd5(byte[] bytes) throws Exception {
		return DigestUtils.md5Hex(bytes);
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

	/**
	 * 计算盐值
	 * 
	 * @param sign2
	 * @param sign1
	 * @return
	 */
	public static String sign(String sign2, String sign1) {
		int[] a = new int[256];
		int[] p = new int[256];
		StringBuffer sb = new StringBuffer();
		int v = sign2.length();
		for (int q = 0; q < 256; q++) {
			a[q] = sign2.charAt(q % v);
			p[q] = q;
		}
		for (int u = 0, q = 0; q < 256; q++) {
			u = (u + p[q] + a[q]) % 256;
			int t = p[q];
			p[q] = p[u];
			p[u] = t;
		}
		for (int i = 0, u = 0, q = 0; q < sign1.length(); q++) {
			i = (i + 1) % 256;
			u = (u + p[i]) % 256;
			int t = p[i];
			p[i] = p[u];
			p[u] = t;
			int k = p[((p[i] + p[u]) % 256)];
			sb.append(Character.toString((char) (sign1.charAt(q) ^ k)));
		}
		return base64Sign(sb.toString());
	}

	/**
	 * 编码盐值
	 * 
	 * @param sign
	 * @return
	 */
	private static String base64Sign(String sign) {
		String s = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
		int e = 0, o = 0, n = 0, i = 0;
		StringBuffer sb = new StringBuffer();
		for (int a = sign.length(); a > e;) {
			o = 255 & sign.charAt(e++);
			if (e == a) {
				sb.append(s.charAt(o >> 2));
				sb.append(s.charAt((3 & o) << 4));
				sb.append("==");
				break;
			}
			n = sign.charAt(e++);
			if (e == a) {
				sb.append(s.charAt(o >> 2));
				sb.append(s.charAt((3 & o) << 4 | (240 & n) >> 4));
				sb.append(s.charAt((15 & n) << 2));
				sb.append("=");
				break;
			}
			i = sign.charAt(e++);
			sb.append(s.charAt(o >> 2));
			sb.append(s.charAt((3 & o) << 4 | (240 & n) >> 4));
			sb.append(s.charAt((15 & n) << 2 | (192 & i) >> 6));
			sb.append(s.charAt(63 & i));
		}
		return sb.toString();
	}

}
