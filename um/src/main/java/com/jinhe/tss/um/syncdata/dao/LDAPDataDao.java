package com.jinhe.tss.um.syncdata.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.InvalidSearchControlsException;
import javax.naming.directory.InvalidSearchFilterException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.dom4j.Document;
import org.dom4j.Element;

import com.jinhe.tss.framework.exception.BusinessException;
import com.jinhe.tss.um.helper.dto.GroupDTO;
import com.jinhe.tss.um.helper.dto.UserDTO;
import com.jinhe.tss.um.syncdata.SyncDataHelper;
import com.jinhe.tss.util.DateUtil;
import com.jinhe.tss.util.EasyUtils;
import com.jinhe.tss.util.XMLDocUtil;

/** 
 * 从LDAP数据里同步用户组织信息
 */
public class LDAPDataDao implements IOutDataDao {
    
    public final static String DEFAULT_VALUE = "defaultValue";
    
    /** 组需要的属性  */
    public final static String APPLICATION_ID_GROUP = "applicationId";
    public final static String DESCRIPTION_GROUP = "description";
    public final static String GROUP_ORDER_GROUP = "groupOrder"; 
    
    /** 用户需要的属性 */
    public final static String APPLICATION_ID_USER = "applicationId";
    public final static String LOGIN_NAME_USER     = "loginName";
    public final static String PASSWORD_USER       = "password";
    public final static String SEX_USER            = "sex";
    public final static String BIRTHDAY_USER       = "birthday";
    public final static String EMPLOYEE_NO_USER    = "employeeNo";
    public final static String USER_ORDER_USER     = "userOrder"; 
 
    private static final String GROUP_FILTER_STR = "OU=*";
    private static final String USER_FILTER_STR  = "CN=*";

    /*  otherAppUserId类似：CN=李文斌,OU=行政政法处,OU=省厅,O=GZCZ   */
    public UserDTO getUser(Map<String, String> paramsMap, String otherAppUserId){
        List<?> list = getOtherUsers( paramsMap,  paramsMap.get(SyncDataHelper.SINGLE_USER), 
                otherAppUserId.substring(otherAppUserId.indexOf(",") + 1),
                otherAppUserId.substring(0, otherAppUserId.indexOf(",")));
        
        if(!EasyUtils.isNullOrEmpty(list)) {
            return (UserDTO)list.get(0);
        }
        return null;
    }
    
    private DirContext getConnection(Map<String, String> map){
    	// 初始化参数设置
        Hashtable<String, String>  env = new Hashtable<String, String> ();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.PROVIDER_URL, map.get(SyncDataHelper.URL));
        env.put(Context.SECURITY_PRINCIPAL, map.get(SyncDataHelper.USERNAME));
        env.put(Context.SECURITY_CREDENTIALS, map.get(SyncDataHelper.PASSWORD));

        // 连接到数据源
        DirContext conn = null;
        try {
            conn = new InitialDirContext(env);
        } catch (Exception e) {
            throw new BusinessException("连接LDAP失败", e);
        }
        return conn;
    }

    /* 
     * attributes格式如：
     *  <attributes>
            <applicationId defaultValue="OA">applicationId</applicationId>
            <id defaultValue="">id</id>
            <parentGroupId defaultValue="">parentGroupId</parentGroupId>
            <groupName defaultValue="">groupName</groupName>
            <description defaultValue="">description</description>
            <groupOrder defaultValue="">groupOrder</groupOrder>
        </attributes> 
     * @see com.jinhe.tss.um.syncdata.dao.IOutDataDao#getOtherGroups(java.lang.Object[])
     */
	public List<?> getOtherGroups(Map<String, String> paramsMap, String attributes, String groupId) {
        Map<String, String> attributesMap = new HashMap<String, String>();
        Map<String, String> defaultValues = new HashMap<String, String>();
        for (Iterator<?> it = XMLDocUtil.dataXml2Doc(attributes).getRootElement().elementIterator(); it.hasNext();) {
            Element element = (Element) it.next();
            attributesMap.put(element.getName(), element.getText());
            defaultValues.put(element.getName(), element.attributeValue(DEFAULT_VALUE));
        }
        
        List<GroupDTO> items = new ArrayList<GroupDTO>();
        try {
        	DirContext conn =  getConnection(paramsMap);
            NamingEnumeration<SearchResult> en = ldapSearch(conn, groupId, GROUP_FILTER_STR);         
            while (en != null && en.hasMoreElements()) {
                SearchResult searchResult = en.next();
                String dn = searchResult.getName();

                // 组合全路径
                dn = (dn != null && !"".equals(dn)) ? (dn + "," + groupId) : groupId;
                
                if (dn.indexOf("OU=") < 0)  continue;

                GroupDTO group = new GroupDTO();

                // 获得组的属性
                group.setId(getGroupId(dn));
                group.setName(getGroupName(dn));
                group.setParentId(getParentGroupId(dn));
                
                Attributes attrs = searchResult.getAttributes();
                // applictionId
                if (attrs.get(attributesMap.get(APPLICATION_ID_GROUP)) != null) {
                    group.setApplicationId(getValueFromAttribute(attrs.get(attributesMap.get(APPLICATION_ID_GROUP))));
                }else{
                    group.setApplicationId(defaultValues.get(APPLICATION_ID_GROUP)); 
                }
                // description
                if (attrs.get(attributesMap.get(DESCRIPTION_GROUP)) != null) {
                    group.setDescription(getValueFromAttribute(attrs.get(attributesMap.get(DESCRIPTION_GROUP))));
                }
                // groupOrder
                if (attrs.get(attributesMap.get(GROUP_ORDER_GROUP).toString()) != null) {
                    group.setSeqNo(Integer.valueOf(getValueFromAttribute(attrs.get(attributesMap.get(GROUP_ORDER_GROUP)))));
                }
                items.add(group);
            }
        } catch (NamingException e) {           
            throw new BusinessException("获取外部用户组失败！",e);
        }
        return items;
    }

    public List<?> getOtherUsers(Map<String, String> paramsMap, String attributes, String groupId, Object...otherParams) {
        String filterString =  otherParams.length > 0 ? (String)otherParams[0] : USER_FILTER_STR;
        
        Document doc = XMLDocUtil.dataXml2Doc(attributes);
        Map<String, String> param = new HashMap<String, String>();
        Map<String, String> defaultValues = new HashMap<String, String>();
        Map<String, Integer> seqNoMap = new HashMap<String, Integer>();
        
        for (Iterator<?> it = doc.getRootElement().elementIterator(); it.hasNext();) {
            Element element = (Element) it.next();
            param.put(element.getName(), element.getText());
            defaultValues.put(element.getName(), element.attribute(DEFAULT_VALUE).getText());
        }
        
        List<UserDTO> items = new ArrayList<UserDTO>();
        Map<String, Object> loginNameCache = new HashMap<String, Object> ();
        Map<String, Object>  dnCache = new HashMap<String, Object> ();
        // 数据查询
        try {
        	DirContext conn =  getConnection(paramsMap);
            NamingEnumeration<SearchResult> en = ldapSearch(conn, groupId, filterString);         
            while (en != null && en.hasMoreElements()) {
                SearchResult sr = en.next();
                String dn = sr.getName();
                
                // 组合全路径
                dn = dn + "," + groupId;
                if(dnCache.containsKey(dn))
                    continue;
                
                Attributes attrs = sr.getAttributes();
                
                if (attrs.get("sn") == null){
                    continue;
                }
                
                UserDTO user = new UserDTO();
                user.setId(dn);
                user.setGroupId(getGroupId(dn));                
                if (attrs.get("sn") != null) {
                    user.setUserName(getNameValueFromAttribute( attrs.get("sn")));
                }
                
                // 获得用户的属性              
                // applictionId
                if (attrs.get(param.get(APPLICATION_ID_USER).toString()) != null) {
                    user.setApplicationId(getValueFromAttribute(attrs.get(param.get(APPLICATION_ID_USER))));
                }else{
                    user.setApplicationId(defaultValues.get(APPLICATION_ID_USER).toString());
                }
                // loginName
                if (attrs.get(param.get(LOGIN_NAME_USER).toString()) != null) {
                    String uid_in_ldap = getNameValueFromAttribute(attrs.get(param.get(LOGIN_NAME_USER)));
                    // uid简称 有可能重名，重名只导入第一个
                    if(loginNameCache.containsKey(uid_in_ldap)) {
                        continue;
                    }
                    user.setLoginName(uid_in_ldap);
                }else{
                    user.setLoginName(dn);
                }
                // password
                if (attrs.get(param.get(PASSWORD_USER).toString()) != null) {
                    user.setPassword(getValueFromAttribute(attrs.get(param.get(PASSWORD_USER))));
                }
                // sex
                if (attrs.get(param.get(SEX_USER).toString()) != null) {
                    user.setSex(getValueFromAttribute(attrs.get(param.get(SEX_USER))));
                }
                // birthday
                if (attrs.get(param.get(BIRTHDAY_USER).toString()) != null) {
                    user.setBirthday(DateUtil.parse(getValueFromAttribute(attrs.get(param.get(BIRTHDAY_USER)))));
                }
                // employeeNo
                if (attrs.get(param.get(EMPLOYEE_NO_USER).toString()) != null) {
                    user.setEmployeeNo(getValueFromAttribute(attrs.get(param.get(EMPLOYEE_NO_USER))));
                }
                // userOrder
                if (attrs.get(param.get(USER_ORDER_USER).toString()) != null) {
                    user.setSeqNo(Integer.valueOf(getValueFromAttribute(attrs.get(param.get(USER_ORDER_USER)))));
                } else {
                    Integer seqNo = (Integer)seqNoMap.get(user.getGroupId());
                    user.setSeqNo(seqNo == null ? new Integer(1) : new Integer(seqNo.intValue() + 1));
                    seqNoMap.put(user.getGroupId(), user.getSeqNo()); //将排序号放入map中以备下次保存同组的用户时可以取到 ＋ 1
                }
                
                if(null != user){
                    items.add(user);
                    dnCache.put(dn, null);
                    loginNameCache.put(user.getLoginName(), null);
                }
            }
        } catch (NamingException e) {           
            throw new BusinessException("获取外部用户失败！",e);
        }
        return items;
    }

    /**
     * <p>
     * LDAP查询
     * </p>
     * @param ctx
     * @param searchBase
     * @param filterString
     * @return
     */
    private NamingEnumeration<SearchResult> ldapSearch(DirContext ctx, String searchBase, String filterString) {
        if (ctx == null)
            return null;

        SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        try {
            return ctx.search(searchBase, filterString, constraints);
        } catch (InvalidSearchFilterException e1){
            throw new BusinessException("Filter不合法", e1);
        } catch (InvalidSearchControlsException e2){
            throw new BusinessException("Constrains不合法", e2);
        } catch (NamingException e) {
            throw new BusinessException("外部组ID不合法", e);
        } 
    }

    /**
     * @param dn 
     *         类似：CN=李文斌,OU=行政政法处,OU=省厅,O=GZCZ
     * @return 
     *         OU=行政政法处,OU=省厅,O=GZCZ
     */
    private String getGroupId(String dn) {
        int position = -1;
        if ((position = dn.indexOf("OU=")) >= 0) {
            return dn.substring(position);
        }
        if ((position = dn.indexOf("O=")) >= 0) {
            return dn.substring(position);
        } 
        return null;
    }

    /**
     * @param dn 
     *         类似：OU=行政政法处,OU=省厅,O=GZCZ
     * @return 
     *         OU=省厅,O=GZCZ
     */
    private String getParentGroupId(String dn) {
        int position = dn.indexOf("OU=");
        if (position >= 0) {
            String selfId = dn.substring(position);
            if ((position = selfId.indexOf(",OU=")) >= 0) {
                return selfId.substring(position + 1);
            } 
            //可能","号后面会多一个空格
            if ((position = selfId.indexOf(", OU=")) >= 0) {
                return selfId.substring(position + 2);
            } 
        }
        return null;
    }

    /**
     * @param dn 
     *         类似：CN=李文斌,OU=行政政法处,OU=省厅,O=GZCZ
     * @return 
     *         行政政法处
     */
    private String getGroupName(String dn) {
        int position = dn.indexOf("OU=");
        if (position >= 0) {
            String temp = dn.substring(position);
            if ((position = temp.indexOf(",")) >= 0) {
                return temp.substring(3, position); //=号和,号之间的就是groupName
            } 
        } 
        return null;
    }
    
    private String getValueFromAttribute(javax.naming.directory.Attribute attr){
    	String attrString = attr.toString();
        return attrString.substring(attrString.indexOf(":") + 1);
    }

    private String getNameValueFromAttribute(javax.naming.directory.Attribute attr){
    	String attrString = attr.toString();
        return attrString.substring(attrString.indexOf(":") + 2);
    }
}

