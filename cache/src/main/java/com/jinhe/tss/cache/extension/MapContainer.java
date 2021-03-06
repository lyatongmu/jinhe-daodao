/* ==================================================================   
 * Created [2009-4-27 下午11:32:55] by Jon.King 
 * ==================================================================  
 * TSS 
 * ================================================================== 
 * mailTo:jinpujun@hotmail.com
 * Copyright (c) Jon.King, 2009-2012 
 * ================================================================== 
 */

package com.jinhe.tss.cache.extension;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.jinhe.tss.cache.AbstractContainer;
import com.jinhe.tss.cache.Cacheable;

/**
 * 为了MapContainer能适用多线程，其每步对 map 的操作都必须锁定 map。 <br/>
 * 
 * map本身不支持同步，需要调用Collections.synchronizedMap使其支持。ehcache则没有这个问题。
 * 
 */
public class MapContainer extends AbstractContainer {

	private Map<Object, Cacheable> map;

	public MapContainer(String name) {
		super(name);
		map = Collections.synchronizedMap(new LinkedHashMap<Object, Cacheable>());
	}

	public Cacheable get(Object key) {
		Cacheable o = null;
		synchronized (map) {
			o = (Cacheable) map.get(key);
		}
		return o;
	}

	public Cacheable put(Object key, Cacheable value) {
		synchronized (map) {
			map.put(key, value);
		}
		return value;
	}

	public Cacheable remove(Object key) {
		Cacheable o = null;
		synchronized (map) {
			o = (Cacheable) map.remove(key);
		}
		return o;
	}

	public Set<Object> keySet() {
	    Set<Object> keySet;
	    synchronized (map) {
	        keySet = map.keySet();
	    }
        return keySet;
	}

	public Set<Cacheable> valueSet() {
        HashSet<Cacheable> valueSet;
        synchronized (map) {
            valueSet = new HashSet<Cacheable>(map.values());
        }
        return valueSet;
    }
	
	public Cacheable getByAccessMethod(int accessMethod) {
	    Cacheable item;
	    synchronized (map) {
	        item = super.getByAccessMethod(accessMethod);
        }
        return item;
	}
	
    public void clear() {
        synchronized (map) {
            map.clear();
        }
    }

    public int size() {
        int size = 0;
        synchronized (map) {
            size = map.size();
        }
        return size;
    }
}
