package com.jinhe.tss.um.syncdata.dao;

import java.util.List;
import java.util.Map;

import com.jinhe.tss.um.helper.dto.UserDTO;

/** 
 * 从外来库同步数据
 */
public interface IOutDataDao {
    
    /**
     * 根据配置信息和传入的参数获取外来库的用户组织信息。
     * 注：数据需要保持原先的层次结构，否则保存组的时候如果先保存了子组将会找不到其父组的ID
     * @param paramsMap 
     * @param groupId 
     * @return
     */
    List<?> getOtherGroups(Map<String, String> paramsMap, String sql, String groupId);
    
    /**
     * 根据配置信息和传入的参数获取外来库的用户信息
     * @param paramsMap
     * @param sql 
     * @param groupId 
     * @return
     */
    List<?> getOtherUsers(Map<String, String> paramsMap, String sql, String groupId, Object...otherParams);

    /**
     * 获取单个用户信息
     * @param param
     * @param otherAppUserId
     * @return
     */
    UserDTO getUser(Map<String, String> paramsMap, String otherAppUserId);
}

