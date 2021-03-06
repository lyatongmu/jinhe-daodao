/* ==================================================================   
 * Created [2006-6-19] by Jon.King 
 * ==================================================================  
 * TSS 
 * ================================================================== 
 * mailTo:jinpujun@hotmail.com
 * Copyright (c) Jon.King, 2012-2015 
 * ================================================================== 
*/
package com.jinhe.tss.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.helpers.Loader;

/** 
 * <p> URLUtil.java </p> 
 * 
 * 相对路径转换成绝对路径
 * 
 * @author Jon.King 2007-4-8
 */
public class URLUtil {
	/**
	 * 获取资源文件的绝对路径
	 * 
	 * @param file
	 * @return
	 */
	public static URL getResourceFileUrl(String file) {
		URL url = Loader.getResource(file);
		if (url == null) {
			url = ClassLoader.class.getResource(file);
		}
		return url;
	}
    
    /**
     * 此处利用了包目录“com/jinhe/tss”来定位。
     * 另外如websphere下的classes目录默认跑到AppServer/properties了，需要加“com/jinhe/tss”帮助定位
     */
    private static URL getOnePathUrl(){
        return URLUtil.getResourceFileUrl("com/jinhe/tss"); 
    }
    
    /**
     * <p>
     * 获取Web文件的绝对路径
     * </p>
     * @param file web文件的相对路径，相对与"WEB-INF"的父目录
     * @return 
     */
    public static URL getWebFileUrl(String file) {
        URL onePathUrl = getOnePathUrl();
        
        String path = onePathUrl.getPath();
        int indexOf = path.lastIndexOf("WEB-INF");
        if(indexOf < 0) {
            indexOf = path.lastIndexOf("target") + 7; // 没有WEB-INF目录，可能是单元测试环境
        }
        path = path.substring(0, indexOf) + file;
        
        try {
            onePathUrl = new URL(onePathUrl.getProtocol(), null, 0, path);
        } catch (MalformedURLException e) {
            throw new RuntimeException("getWebFileUrl方法定位path: " + path + " 失败", e);
        }
        return onePathUrl;
    }
    
    /**
     * 定位项目中classes目录路径
     * @return
     */
    public static URL getClassesPath() {
        URL onePathUrl = getOnePathUrl();
        String path = onePathUrl.getPath();
        path = path.substring(0, path.length() - 9);
        
        try {
            onePathUrl = new URL(onePathUrl.getProtocol(), null, 0, new File(path).getParent());
        } catch (MalformedURLException e) {
            throw new RuntimeException("getClassesPath方法定位path: " + path + " 失败", e);
        }
        return onePathUrl;
    }
    
    /**
     * 定位项目中lib目录路径
     * @return
     */
    public static URL getLibPath() {
        URL onePathUrl = getOnePathUrl();
        String path = onePathUrl.getPath();
        path = path.substring(0, path.lastIndexOf("WEB-INF") + 7) + "/lib/" ;
        try {
            onePathUrl = new URL(onePathUrl.getProtocol(), null, 0, path);
        } catch (MalformedURLException e) {
            throw new RuntimeException("getLibPath方法定位path: " + path + " 失败", e);
        }
        return onePathUrl;
    }
}
