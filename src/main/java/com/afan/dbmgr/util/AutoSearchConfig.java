package com.afan.dbmgr.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 尝试在工作目录，jar同级目录下找到指定格式结尾的文件
 * @author cf
 *
 */
public class AutoSearchConfig {
	
	public static String searchFilePath(String fileName){
		List<File> files = search(fileName, true);
		if(files!=null && files.size()>0){
			return files.get(0).getPath();
		}
		return null;
	}
	
	public static List<File> search(String fileName){
		return search(fileName, false);
	}
	
	/**
	 * 
	 * @param fileName
	 * @param searchReturn找到就返回
	 * @return
	 */
	public static List<File> search(String fileName, boolean searchReturn){
		try{
			String rootPath = AutoSearchConfig.class.getProtectionDomain().getCodeSource().getLocation().getFile();
			rootPath = java.net.URLDecoder.decode(rootPath, "UTF-8");
			File rootFile = new File(rootPath);
			if(rootFile.isFile()){
				rootPath = rootFile.getParent();
				//引用的方式找到的是lib下的jar包
				rootFile =  new File(rootPath);
				if("lib".equals(rootFile.getName())){
					rootPath = rootFile.getParent();
				}
			}
			List<File> result = new ArrayList<File>();
			findFiles(rootPath, result, fileName, searchReturn);
			return result;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	private static void findFiles(String currentDir, List<File> fileList, String fileName, boolean searchReturn) {
		File dir = new File(currentDir);
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}
		for (File file : dir.listFiles()) {
			if(fileList.size()>0)
				break;
			if (file.isDirectory()) {
				findFiles(file.getAbsolutePath(), fileList, fileName, searchReturn);
			} else {
				if (file.getAbsolutePath().endsWith(fileName)) {
					fileList.add(file);
					if(searchReturn)
						return;
				}
			}
		}
	}

}
