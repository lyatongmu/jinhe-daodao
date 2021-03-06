package com.jinhe.tss.um.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.jinhe.tss.framework.persistence.IEntity;
import com.jinhe.tss.framework.persistence.entityaop.OperateInfo;
import com.jinhe.tss.framework.web.dispaly.tree.ITreeNode;
import com.jinhe.tss.framework.web.dispaly.tree.TreeAttributesMap;
import com.jinhe.tss.framework.web.dispaly.xform.IXForm;
import com.jinhe.tss.um.UMConstants;
import com.jinhe.tss.util.BeanUtil;
import com.jinhe.tss.util.DateUtil;

/**
 * 权限转授策略(Sub Authorize Strategy)域对象。
 * 策略可以授予用户、用户组、也可以授予角色，或者三者兼有。
 */
@Entity
@Table(name = "um_sub_authorize")
@SequenceGenerator(name = "SubAuthorize_sequence", sequenceName = "SubAuthorize_sequence", initialValue = 1000, allocationSize = 10)
public class SubAuthorize extends OperateInfo implements IEntity, ITreeNode, IXForm {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "SubAuthorize_sequence")
	private Long   id;   // 策略ID：策略主键ID
    
    @Column(nullable = false)  
	private String name; // 名称:策略名称
	private Date   startDate;  // 开始时间 
	private Date   endDate;    // 结束时间 
	
	@Column(length = 1000)  
	private String description;// 描述:对策略的描述
    
    private Integer disabled = UMConstants.FALSE;// 策略状态 1-停用, 0-启用 
 
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
 
	public String getDescription() {
		return description;
	}
 
	public void setDescription(String description) {
		this.description = description;
	}
 
	public Date getEndDate() {
		return endDate;
	}
 
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
 
	public String getName() {
		return name;
	}
 
	public void setName(String name) {
		this.name = name;
	}
 
	public Integer getDisabled() {
		return disabled;
	}
 
	public void setDisabled(Integer state) {
		this.disabled = state;
	}
 
	public Date getStartDate() {
		return startDate;
	}
 
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
 
	public Map<String, Object> getAttributesForXForm() {
		Map<String, Object> map = new HashMap<String, Object>();
		BeanUtil.addBeanProperties2Map(this, map);
		
		map.put("startDate", DateUtil.format(startDate));
		map.put("endDate",   DateUtil.format(endDate));
		return map;
	}
 
	public TreeAttributesMap getAttributes() {
		TreeAttributesMap map = new TreeAttributesMap(id, name);
		map.put("disabled", disabled);

		if (UMConstants.FALSE.equals(disabled)) {// 启用
			map.put("icon", UMConstants.START_STRATEGY_TREENODE_ICON);
		} else {
			map.put("icon", UMConstants.STOP_STRATEGY_TREENODE_ICON);
		}
		
		super.putOperateInfo2Map(map);
		return map;
	}
 
}
