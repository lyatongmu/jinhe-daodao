/* ==================================================================   
 * Created [2007-1-3] by Jon.King 
 * ==================================================================  
 * TSS 
 * ================================================================== 
 * mailTo:jinpujun@hotmail.com
 * Copyright (c) Jon.King, 2012-2015 
 * ================================================================== 
 */

package com.jinhe.tss.cache;

/**
 * 缓冲池容器抽象超类。
 * 
 */
public abstract class AbstractContainer implements Container {

	protected String name;

	public AbstractContainer(String name) {
		this.name = name;
	}

	public Cacheable getByAccessMethod(int accessMethod) {
	    if(size() == 0) {
	        return null;
	    }
	    
		Object item = null;
		switch (accessMethod) {
		case ACCESS_LRU:
			long lastAccessed = 0;
			for (Cacheable temp : valueSet()) {
				long life = System.currentTimeMillis() - temp.getAccessed();
				if (life > lastAccessed) {
					lastAccessed = life;
					item = temp; // 找出最近最长时间使用的
				}
			}
			break;
		case ACCESS_LFU:
			int minHit = 999999999;
			for (Cacheable temp : valueSet()) {
				if (minHit < temp.getHit()) {
					minHit = temp.getHit();
					item = temp; // 找出最不常使用的
				}
			}
			break;
		case ACCESS_RANDOM:
			item = valueSet().toArray()[(int) ( size() * Math.random())]; // 随机
			break;
		case ACCESS_FIFO:
			item = valueSet().toArray()[0];
			break;
		case ACCESS_LIFO:
		default:
			item = valueSet().toArray()[(size() - 1)];
		}
		
		return (Cacheable)item;
	}

	public Cacheable removeByAccessMethod(int accessMethod) {
		Cacheable o = getByAccessMethod(accessMethod);
		return remove(o.getKey());
	}
 
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("\n----------------【" + name + "】池中数据项列表，共【" + size() + "】个 --------------\n");
		for (Cacheable item : valueSet()) {
			if (item != null) {
				sb.append("  key: ").append(item.getKey());
				sb.append(", value: ").append(item.getValue());
				sb.append(", hit: ").append(item.getHit()).append("\n");
			}
		}
		return sb.append("----------------------------------- END ---------------------------").toString();
	}
}
