package com.jinhe.tss.framework.sso;

import java.util.HashMap;
import java.util.Map;

import com.jinhe.tss.framework.sso.IOperator;
 
public class DemoOperator implements IOperator {
 
    private static final long serialVersionUID = 1L;

    private Map<String, Object> attributesMap = new HashMap<String, Object>();

    private Long userId;

    public DemoOperator(Long userId) {
        this.userId = userId + 1;
        attributesMap.put("id", this.userId);
        attributesMap.put("loginName", getLoginName());
        attributesMap.put("userName", getUserName());
        attributesMap.put("authenticateMethod", getAuthenticateMethod());
    }

    public Object getAttribute(String name) {
        return attributesMap.get(name);
    }

    public Map<String, Object> getAttributesMap() {
        return attributesMap;
    }

    public String getAuthenticateMethod() {
        return DemoUserIdentifier.class.getName();
    }

    public Long getId() {
        return userId;
    }

    public String getLoginName() {
        return "Jon.King";
    }

    public String getUserName() {
        return "JinPujun";
    }

    public boolean isAnonymous() {
        return false;
    }

    public void setId(Long id){
    	this.userId = id;
    }

}
