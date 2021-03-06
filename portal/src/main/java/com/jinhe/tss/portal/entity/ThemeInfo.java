package com.jinhe.tss.portal.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.jinhe.tss.framework.persistence.IEntity;

/** 
 * 主题信息表,包含主题下各个门户结构的布局修饰内容
 */
@Entity
@Table(name = "pms_theme_info")
public class ThemeInfo implements IEntity {
	
	@EmbeddedId
    private ThemeInfoId id;  // 复合主键：主题ID ＋ 门户结构ID
    
    /**
     * 如果门户结构为Portlet引用，则此项为空
     */
    private Long    layoutId;      // 布局器ID
    private String  layoutName;    // 布局器名称
    
    private Long    decoratorId;   // 修饰器Id
    private String  decoratorName; // 修饰器名称
    
    private String  parameters;    // Portlet、修饰器、布局器的实例化时自定义参数值

	public ThemeInfoId getId() {
		return id;
	}

	public Long getLayoutId() {
		return layoutId;
	}

	public String getLayoutName() {
		return layoutName;
	}

	public Long getDecoratorId() {
		return decoratorId;
	}

	public String getDecoratorName() {
		return decoratorName;
	}

	public String getParameters() {
		return parameters;
	}

	public void setId(ThemeInfoId id) {
		this.id = id;
	}

	public void setLayoutId(Long layoutId) {
		this.layoutId = layoutId;
	}

	public void setLayoutName(String layoutName) {
		this.layoutName = layoutName;
	}

	public void setDecoratorId(Long decoratorId) {
		this.decoratorId = decoratorId;
	}

	public void setDecoratorName(String decoratorName) {
		this.decoratorName = decoratorName;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
}