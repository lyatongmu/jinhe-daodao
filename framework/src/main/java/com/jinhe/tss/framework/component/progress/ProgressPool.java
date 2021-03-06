package com.jinhe.tss.framework.component.progress;

import java.util.HashMap;
import java.util.Map;

/**
 * <p> 进度条池 </p>
 * <p>
 * 根据进度对象的isConceal属性来判断执行还是中止当前的任务。
 * </p>
 */
public class ProgressPool {
    
	/**
	 * 执行中的进度条池
	 */
	private static Map<String, Progress> progressMap = new HashMap<String, Progress>();
	
	/**
	 * 放入进度条对象
	 */
	public static void putSchedule(String code, Progress obj){
		progressMap.put(code, obj);
	}
	
	public static Progress getSchedule(String code){
		return progressMap.get(code);
	}
	
	public static Progress removeSchedule(String code){
		return progressMap.remove(code);
	}
	
	
	/**
	 * 执行中的带进度反馈的业务对象池
	 */
	private static Map<String, FeedbackProgressable> feedbackProgressableMap = new HashMap<String, FeedbackProgressable>();//
	
	public static void putFPObject(String code, FeedbackProgressable obj){
	    feedbackProgressableMap.put(code, obj);
    }
    
    public static FeedbackProgressable getFPObject(String code){
        return feedbackProgressableMap.get(code);
    }
    
    public static FeedbackProgressable removeFPObject(String code){
        return feedbackProgressableMap.remove(code);
    }

}
