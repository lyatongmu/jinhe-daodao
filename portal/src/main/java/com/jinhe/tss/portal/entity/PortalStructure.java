package com.jinhe.tss.portal.entity;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.jinhe.tss.framework.exception.BusinessException;
import com.jinhe.tss.framework.persistence.IEntity;
import com.jinhe.tss.framework.persistence.entityaop.IDecodable;
import com.jinhe.tss.framework.persistence.entityaop.OperateInfo;
import com.jinhe.tss.framework.web.dispaly.tree.ILevelTreeNode;
import com.jinhe.tss.framework.web.dispaly.tree.TreeAttributesMap;
import com.jinhe.tss.framework.web.dispaly.xform.IXForm;
import com.jinhe.tss.portal.PortalConstants;
import com.jinhe.tss.portal.entity.permission.PortalResourceView;
import com.jinhe.tss.um.permission.IResource;
import com.jinhe.tss.util.BeanUtil;
import com.jinhe.tss.util.URLUtil;

/**
 * 门户结构实体：自引用结构，不同的节点分别代表Portal、页面、版面、Portlet实例等
 */
@Entity
@Table(name = "pms_portal_structure", uniqueConstraints = { 
        @UniqueConstraint(name="MULTI_NAME_PortalStructure", columnNames = { "parentId", "name" })
})
@SequenceGenerator(name = "portalstructure_sequence", sequenceName = "portalstructure_sequence", initialValue = 1, allocationSize = 1)
public class PortalStructure extends OperateInfo implements IEntity, ILevelTreeNode, IXForm, IResource, IDecodable {
    
    public static final int TYPE_PORTAL           = 0; //Portal节点
    public static final int TYPE_PAGE             = 1; //页面节点
    public static final int TYPE_SECTION          = 2; //版面节点
    public static final int TYPE_PORTLET_INSTANCE = 3; //Portlet实例节点
    
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "portalstructure_sequence")
    private Long id;  
	
	@Column(nullable = false)
    private Long parentId; //父节点编号
    
    @Column(nullable = false)
    private Long portalId; //所属门户编号

    /**
     * 节点类型：
     * <li>0－结构根节点（相当于门户，但也是一个版面）；
     * <li>1－页面：decorator表示修饰器、definer表示布局器
     * <li>2－版面：decorator表示修饰器、definer表示布局器
     * <li>3－Portlet实例：decorator表示修饰器、definer表示Portlet
     */
    @Column(nullable = false)
    private Integer type;
    
    @Column(nullable = false)
    private String  name;   // 节点名称：门户名称/页面名称/版面名称/Portlet实例名称
    private String  code;   // 门户结构节点的代码
   
    private Long   definerId;    // 布局器/Portlet编号
    private String definerName;  // 布局器/Portlet名称
    
    /**
     * 修饰器信息不需要对应数据库，修饰器信息在主题信息表中
     */
    @Transient private Long   decoratorId;   // 修饰器编号
    @Transient private String decoratorName; // 修饰器名称

    private String parameters;  // Portlet、修饰器实例化时自定义参数值
    
    @Column(length = 4000)
    private String supplement;  // Portal和页面全局附加脚本和样式表定义信息
    private String description; // 门户、版面、Portlet应用、菜单应用等节点描述信息
    
    @Column(nullable = false)
    private Integer seqNo;   // 顺序号
    private String  decode;  // 层码
    private Integer levelNo; // 层次值

    private Integer disabled = PortalConstants.FALSE;  // 是否停用：0－启用；1－停用
    
    public String toString(){
        return "(id:" + this.id + ", name:" + this.name + 
                ", code:" + this.code +  
        		", parentId:" + this.parentId + ")"; 
    }
    
    @Transient Collection<PortalStructure> children = new LinkedHashSet<PortalStructure>();

    public Collection<PortalStructure> getChildren() {
        return children;
    }

    public void addChild(PortalStructure ps) {
        children.add(ps);
    }
 
    public boolean isRootPortal() { return TYPE_PORTAL == this.type.intValue(); }
    public boolean isPage()       { return TYPE_PAGE == this.type.intValue(); }
    public boolean isSection()    { return TYPE_SECTION == this.type.intValue(); }
    public boolean isPortletInstanse() { return TYPE_PORTLET_INSTANCE == this.type.intValue(); }

    /**
     * 将一个门户结构节点下所有的子节点递归放入到各自的父节点下
     * 
     * <br>
     * |-门户_1 <br>
     * ........|- 页面_1 <br>
     * ................|- portlet应用_1_1 <br>
     * ................|- 版面_1_1 <br>
     * ..........................|- 版面_1_1_2 <br>
     * ......................................|- portlet应用_1_1_2_1 <br>
     * ..........................|- portlet应用_1_1_2 <br>
     * ........|- 页面_1 <br>
     * ........|- 页面_2 <br>
     * 
     * @param list
     */
    public void compose(List<PortalStructure> list){
        if(this.type.equals(TYPE_PORTLET_INSTANCE)){
            throw new BusinessException("当前门户结构是portlet实例,不能进行该操作!");
        }

        Map<Long, PortalStructure> map = new HashMap<Long, PortalStructure>();
        map.put(this.getId(), this);

        for ( PortalStructure entity : list ) {
            map.put(entity.getId(), entity);
        }
        
        for ( PortalStructure entity : list ) {
            Long parentId = entity.getParentId();
            PortalStructure parent = map.get(parentId);

            if (parentId.equals(this.id)) {
                this.addChild(entity);
            } else {
                parent.addChild(entity);
            }
        }
    }
    
    @Transient private List<Navigator> menus = new ArrayList<Navigator>();
    
    public List<Navigator> getMenus(){
        return this.menus;
    }
    
    /**
     * |-门户 <br>
     * ........|- 菜单 <br>
     * ................|- 菜单项 <br>
     * ........................|- 菜单项 <br>
     * 
     * @param list
     */
    public void composeMenus(List<Navigator> list){
        Map<Long, Navigator> map = new HashMap<Long, Navigator>();

        for ( Navigator entity : list ) {
            map.put(entity.getId(), entity);
        }
        
        for ( Navigator entity : list ) {
            if(Navigator.TYPE_MENU.equals(entity.getType())){
                this.getMenus().add(entity);
            } 
            else {
                Navigator parent = map.get(entity.getParentId());
                parent.addChild(entity);
            }            
        }
    }
    
    public File getPortalResourceFileDir(){
        URL url = URLUtil.getWebFileUrl(PortalConstants.PORTAL_MODEL_DIR);
        return new File(url.getPath() + "/" + code + "_" + portalId);
    }
    
    public static File getPortalResourceFileDir(String code, Long portalId){
        return getPortalResourceFileDir(code + "_" + portalId);
    }
    
    public static File getPortalResourceFileDir(String path){
        URL url = URLUtil.getWebFileUrl(PortalConstants.PORTAL_MODEL_DIR);
        return new File(url.getPath() + "/" + path);
    }
    
    public Map<String, Object> getAttributesForXForm() {
        Map<String, Object> map = new HashMap<String, Object>();
        BeanUtil.addBeanProperties2Map(this, map, "children, menus".split(","));
        return map;
    }

    public TreeAttributesMap getAttributes() {
        TreeAttributesMap map = new TreeAttributesMap(id, name);
        map.put("code", code);
        map.put("type", type);
        map.put("portalId", portalId);
        map.put("disabled", disabled);
        map.put("resourceTypeId", getResourceType());

        boolean disable = PortalConstants.TRUE.equals(disabled);
        switch (type) {
        case 0:
            map.put("icon", "../framework/images/portal" + (disable ? "_2" : "") + ".gif");
            break;
        case 1:
            map.put("icon", "../framework/images/page" + (disable ? "_2" : "") + ".gif");
            break;
        case 2:
            map.put("icon", "../framework/images/section" + (disable ? "_2" : "") + ".gif");
            break;
        case 3:
            map.put("icon", "../framework/images/portlet_instance" + (disable ? "_2" : "") + ".gif");
            break;
        default:
            break;
        }
        
        super.putOperateInfo2Map(map);
        return map;
    }
    
    public String getResourceType() {
        return PortalConstants.PORTAL_RESOURCE_TYPE;
    }
    
    public Class<?> getParentClass() {
        if(this.parentId.equals(PortalConstants.ROOT_ID)) {
            return PortalResourceView.class;
        }
        return getClass();
    }
 
    public String getDecode() {
        return decode;
    }
 
    public Integer getDisabled() {
        return disabled;
    }
 
    public Integer getLevelNo() {
        return levelNo;
    }
 
    public void setDecode(String decode) {
        this.decode = decode;
    }
 
    public void setDisabled(Integer disabled) {
        this.disabled = disabled;
    }
 
    public void setLevelNo(Integer levelNo) {
        this.levelNo = levelNo;
    }
 
    public Long getDecoratorId() {
        return decoratorId;
    }
 
    public void setDecoratorId(Long decoratorId) {
        this.decoratorId = decoratorId;
    }
 
    public String getDecoratorName() {
        return decoratorName;
    }
 
    public void setDecoratorName(String decoratorName) {
        this.decoratorName = decoratorName;
    }
 
    public Long getDefinerId() {
        return definerId;
    }
 
    public void setDefinerId(Long definerId) {
        this.definerId = definerId;
    }
 
    public String getDefinerName() {
        return definerName;
    }
 
    public void setDefinerName(String definerName) {
        this.definerName = definerName;
    }
 
    public Long getId() {
        return id;
    }
 
    public void setId(Long id) {
        this.id = id;
    }
 
    public String getName() {
        return name;
    }
 
    public void setName(String name) {
        this.name = name;
    }
 
    public Long getParentId() {
        return parentId;
    }
 
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
 
    public Integer getSeqNo() {
        return seqNo;
    }
 
    public void setSeqNo(Integer seqNo) {
        this.seqNo = seqNo;
    }
 
    public Integer getType() {
        return type;
    }
 
    public void setType(Integer type) {
        this.type = type;
    }
 
    public String getDescription() {
        return description;
    }
 
    public void setDescription(String description) {
        this.description = description;
    }
 
    public Long getPortalId() {
        return portalId;
    }
 
    public void setPortalId(Long portalId) {
        this.portalId = portalId;
    }
 
    public String getParameters() {
        return parameters;
    }
 
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
 
    public String getCode() {
        return code;
    }
 
    public void setCode(String code) {
        this.code = code;
    }
 
    public String getSupplement() {
        return supplement;
    }
 
    public void setSupplement(String supplement) {
        this.supplement = supplement;
    }
}