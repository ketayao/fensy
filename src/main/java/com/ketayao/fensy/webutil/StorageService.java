package com.ketayao.fensy.webutil;

import com.ketayao.fensy.mvc.RequestContext;

/**
 * 文件存储服务
 */
public class StorageService extends Storage {

	public final static StorageService IMAGE = new StorageService("images");
	public final static StorageService ATTACH = new StorageService("attaches");
	
	private String file_path;
	private String read_path;
	
	public StorageService(String ext){
		this.file_path = RequestContext.root() + 
				"uploads" + java.io.File.separator + 
				ext + java.io.File.separator;
		this.read_path = "/uploads/" + ext + "/";
	}
	
	@Override
	public String getBasePath() {
		return file_path;
	}
	
	public String getReadPath() {
		return read_path;
	}
}
