package com.jinhe.tss.um.service;

import java.util.List;
import java.util.Map;

import com.jinhe.tss.um.entity.SubAuthorize;

/**
 * 权限转授策略相关。
 */
public interface ISubAuthorizeService {

	/**
	 * <p>
	 * 删除权限转授策略
	 * </p>
	 * @param id
	 */
	void deleteStrategy(Long id);

	/**
	 * <p>
	 * 0-停用/1-启用权限转授策略
	 * </p>
	 * @param id
	 * @param disabled
	 */
	void disable(Long id, Integer disabled);
	
	/**
	 * <p>
	 * 新建权限转授策略使用的信息
	 * </p>
	 * @param userId
	 * 			用户id
	 * 				用来过滤该用户可见的用户组、角色列表
	 * @return
	 */
	Map<String, Object> getStrategyInfo4Create();
	
	/**
	 * <p>
	 * 编辑权限转授策略使用的信息
	 * </p>
	 * @param ruleId
	 * 			策略id
	 * @return
	 */
	Map<String, Object> getStrategyInfo4Update(Long ruleId);

	/**
	 * <p>
	 * 查找指定用户创建出来的权限转授策略列表
	 * </p>
	 * @param userId
	 * @return
	 */	
	List<?> getStrategyByCreator();

	/**
	 * <p>
	 * 保存权限转授策略的关系
	 * 策略对角色,策略对用户,策略对用户组	
	 * </p>
	 * @param strategy
	 * @param userIds
	 * @param groupsIds
	 * @param roleIds
	 */
	void saveStrategy(SubAuthorize strategy, String userIds, String groupsIds, String roleIds);

}