package com.onewen.baidupan.model;

/**
 * 云盘文件
 * 
 * @author 梁光运
 * @date 2018年8月23日
 */
public class PanFile {
	private int server_mtime;
	private int category;
	private int unlist;
	private long fs_id;
	private int oper_id;
	private int server_ctime;
	private boolean isdir;
	private int local_mtime;
	private long size;
	private int share;
	private String md5;
	private String path;
	private int local_ctime;
	private String server_filename;

	public int getServer_mtime() {
		return server_mtime;
	}

	public void setServer_mtime(int server_mtime) {
		this.server_mtime = server_mtime;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public int getUnlist() {
		return unlist;
	}

	public void setUnlist(int unlist) {
		this.unlist = unlist;
	}

	public long getFs_id() {
		return fs_id;
	}

	public void setFs_id(long fs_id) {
		this.fs_id = fs_id;
	}

	public int getOper_id() {
		return oper_id;
	}

	public void setOper_id(int oper_id) {
		this.oper_id = oper_id;
	}

	public int getServer_ctime() {
		return server_ctime;
	}

	public void setServer_ctime(int server_ctime) {
		this.server_ctime = server_ctime;
	}

	public boolean isIsdir() {
		return isdir;
	}

	public void setIsdir(boolean isdir) {
		this.isdir = isdir;
	}

	public int getLocal_mtime() {
		return local_mtime;
	}

	public void setLocal_mtime(int local_mtime) {
		this.local_mtime = local_mtime;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public int getShare() {
		return share;
	}

	public void setShare(int share) {
		this.share = share;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getLocal_ctime() {
		return local_ctime;
	}

	public void setLocal_ctime(int local_ctime) {
		this.local_ctime = local_ctime;
	}

	public String getServer_filename() {
		return server_filename;
	}

	public void setServer_filename(String server_filename) {
		this.server_filename = server_filename;
	}

	@Override
	public String toString() {
		return "PanFile [isdir=" + isdir + ", size=" + size + ", path=" + path + "]";
	}

}
