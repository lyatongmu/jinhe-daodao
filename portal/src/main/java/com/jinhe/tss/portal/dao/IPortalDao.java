package com.jinhe.tss.portal.dao;

import java.util.List;

import com.jinhe.tss.framework.persistence.ITreeSupportDao;
import com.jinhe.tss.portal.PortalConstants;
import com.jinhe.tss.portal.entity.Decorator;
import com.jinhe.tss.portal.entity.Layout;
import com.jinhe.tss.portal.entity.PersonalTheme;
import com.jinhe.tss.portal.entity.Portal;
import com.jinhe.tss.portal.entity.PortalStructure;
import com.jinhe.tss.um.permission.filter.PermissionFilter4Branch;
import com.jinhe.tss.um.permission.filter.PermissionTag;

public interface IPortalDao extends ITreeSupportDao<PortalStructure>{
 
    /**
     * 根据portalId获取Portal实体
     * @param id
     * @return
     */
    Portal getPortalById(Long id);
    
    /**
     * 本方法将会被拦截进行资源注册操作，所以一般的保存应该直接调save方法。
     * @param p
     * @return
     */
    PortalStructure savePortalStructure(PortalStructure p);
    
    /**
     * 删除门户节点。 
     * 删除注册资源由拦截器ResourcePermissionInterceptor完成。
     * @param ps
     */
    void deletePortalStructure(PortalStructure ps);
    
    /**
     * 本方法是为了资源权限补齐拦截器ResourcePermissionInterceptor能拦截到门户结构的移动保存操作，
     * 从而可以对移动的资源根据新的父节点进行权限补齐处理。
     * @param ps
     * @return
     */
    PortalStructure movePortalStructure(PortalStructure ps);

    /**
     * 根据父亲节点获取所有子节点。
     * 必须根据decode来排序，以保证层次和顺序结构。
     * @param id
     *         父节点Id
     * @param operationId
     *         操作ID，拦截器用
     * @return
     *         所有子节点
     */
    @PermissionTag(
            resourceType = PortalConstants.PORTAL_RESOURCE_TYPE,
            filter = PermissionFilter4Branch.class)
    List<PortalStructure> getChildrenById(Long id, String operationId);

    /**
     * 获取所有的父亲级节点，一直到根目录。
     * 必须根据decode来排序，以保证层次和顺序结构。
     * @param id
     *         当前节点
     * @param operationId 
     *         操作ID ，拦截器用
     * @return
     */
    @PermissionTag(
            resourceType = PortalConstants.PORTAL_RESOURCE_TYPE,
            filter = PermissionFilter4Branch.class)
    List<PortalStructure> getParentsById(Long id, String operationId);
    
    /**
     * 获取指定门户Portal的所有组成元素
     * @param portalId
     *              门户ID
     * @param currentThemeId 
     * @return
     */
    Object[] getPortalElements(Long portalId, Long currentThemeId);
    
    /**
     * 获取默认的布局器，如果没有默认的则抛出异常
     * @return
     */
    Layout getDefaultLayout();
    
    /**
     * 获取默认的修饰器，如果没有默认的则抛出异常
     * @return
     */
    Decorator getDefaultDecorator();

    /**
     * 根据portalId获取门户结构根节点
     * @param portalId
     * @return
     */
    PortalStructure getRootPortalStructure(Long portalId);

    /**
     * 获取portal下的所有主题
     * @param portalId
     * @return
     */
    List<?> getThemesByPortal(Long portalId);

    /**
     * 获取用户的自定义主题
     * @param portalId
     * @return
     */
    PersonalTheme getPersonalTheme(Long portalId);
}

