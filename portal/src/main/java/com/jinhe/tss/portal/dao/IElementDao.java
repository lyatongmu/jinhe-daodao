package com.jinhe.tss.portal.dao;

import java.util.List;

import com.jinhe.tss.framework.persistence.ITreeSupportDao;
import com.jinhe.tss.portal.entity.ElementGroup;
import com.jinhe.tss.portal.helper.IElement;

/** 
 * 对元素（修饰器/布局器/Portlet）数据存取操作的DAO接口
 */
public interface IElementDao extends ITreeSupportDao<ElementGroup>{
 
    /**
     * 保存一个元素（修饰器/布局器/Portlet）。
     * 注意：本方法将会拦截以实现资源注册，所以普通保存应该直接调用save方法。
     * @param obj
     * @return
     */
    IElement saveElement(IElement obj);

    /**
     * 删除一个元素（修饰器/布局器/Portlet）
     * 删除注册资源由拦截器ResourcePermissionInterceptor完成。
     * @param obj
     * @return
     */
    IElement deleteElement(IElement obj);
    
    /**
     * 将元素（修饰器/布局器/Portlet）移动到指定组下。<br/>
     * 本方法是为了资源权限补齐拦截器ResourcePermissionInterceptor能拦截到门户结构的移动保存操作，
     * 从而可以对移动的资源根据新的父节点进行权限补齐处理。
     * @param id
     * @param groupId
     * @return
     */
    IElement moveTo(IElement element);
    
    /*****************************************************************************************************************
     ************************************ 以下是对元素（修饰器/布局器/Portlet）组的操作 ************************************* 
     *****************************************************************************************************************/
    
    /**
     * 保存一个组。
     * 注意：本方法将会拦截以实现资源注册，所以普通保存应该直接调用save方法。
     * @param group
     * @return
     */
    ElementGroup saveGroup(ElementGroup group);
    /**
     * 删除一个组（配置了回收站的话本方法将被拦截，代之执行回收站中配置的执行方法）
     * @param group
     * @return
     */
    void deleteGroup(ElementGroup group);
 
    /**
     * 获取所有儿子节点
     * @param id
     * @return
     */
    List<ElementGroup> getChildrenById(Long id);

    /**
     * 获取组以及子组下所有的元素，获取portlet用于复制的时候需要根据查看操作进行过滤
     * @param groupId
     * @return
     */
    List<?> getAllElementsByGroup(Long groupId);
    
    /**
     * 只获取当前组下的元素，不包括子组下的元素。
     * @param groupId
     * @return
     */
    List<?> getElementsByGroup( Long groupId );
}

