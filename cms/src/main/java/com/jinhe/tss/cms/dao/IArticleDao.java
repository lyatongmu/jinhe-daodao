package com.jinhe.tss.cms.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.jinhe.tss.cms.entity.Article;
import com.jinhe.tss.cms.entity.Attachment;
import com.jinhe.tss.cms.helper.ArticleQueryCondition;
import com.jinhe.tss.framework.persistence.IDao;
import com.jinhe.tss.framework.persistence.pagequery.PageInfo;

/** 
 * Article的Dao层接口，定义所有Article相关的数据库操作接口
 */
public interface IArticleDao extends IDao<Article> {
 
    /**
     * <p>
     *  根据栏目id获取发布成xml文件(真实发布状态)的文章列表
     * </p>
     * @param channelId
     * @return
     */
    List<?> getPublishedArticleByChannel(Long channelId);

	/**
	 * <p>
	 * 获取栏目文章关联表中选中栏目ID的最大序号
	 * </p>
	 * @param channelId
	 * @return
	 */
	Integer getChannelArticleNextOrder(Long channelId);

	/**
	 * <p>
	 * 根据文章ID获取文章附件列表
	 * </p>
	 * @param articleId
	 * @return
	 */
	Map<String, Attachment> getArticleAttachments(Long articleId);

    /**
     * <p>
     * 根据文章ID获取此文章附件最大的序号
     * </p>
     * @param id
     * @return
     */
    Integer getAttachmentNextOrder(Long id);
    
    /**
     * 获取文章下的指定附件
     * 
     * @param articleId
     * @param seqNo
     * @return
     */
    Attachment getAttachment(Long articleId, Integer seqNo);
 
    /**
     * <p>
     *  获得栏目的文章列表
     * </p>
     * @param channelId
     * @param pageNum
     * @param orderBy
     * @return
     */
    PageInfo getPageList(Long channelId, Integer pageNum, String...orderBy);
 
    /**
     * <p>
     *  条件搜索栏目及其子栏目的文章列表
     * </p>
     * @param condition
     * @return
     */
    PageInfo getSearchArticlePageList(ArticleQueryCondition condition);
 
    /**
     * <p>
     *  获取栏目的分页文章列表，Portlet远程调用时使用
     * </p>
     * @param condition
     * @return
     */
    PageInfo getChannelPageArticleList(ArticleQueryCondition condition);
    
    /**
     * 根据栏目ids，获取这些栏目下的所有文章列表
     * @param condition
     * @param isArchives
     * @return
     */
    PageInfo getArticlesByChannelIds(ArticleQueryCondition condition);
    
    /**
     * <p> 保存文章 </p>
     * @param article
     * @return
     */
    Article saveArticle(Article article);
    
    /**
     * <p>
     * 删除文章
     * </p>
     * @param article
     */
    void deleteArticle(Article article);
    
    /**
     * <p>
     * 获取过期文章列表。
     * 比较过期时间是早于当前时间，以及文章是否为”已发布“状态。其他状态没必要设置为过期。
     * </p>
     * @param now
     * @param channelId
     * @return
     */
    List<Article> getExpireArticlePuburlList(Date now, Long channelId);
}