package com.jinhe.tss.cms.service;

import com.jinhe.tss.cms.entity.Article;
import com.jinhe.tss.cms.helper.ArticleQueryCondition;
import com.jinhe.tss.framework.component.log.Logable;
import com.jinhe.tss.framework.persistence.pagequery.PageInfo;

public interface IArticleService {

    /**
     * 新增/修改文章
     * 
     * @param article
     * @param channelId
     * @param attachList
     * @param tempArticleId
     *            新增的时候上传的附件对象以new Date()为主键，此处的tempArticleId就是这个值
     */
    @Logable(operateTable="文章", operateType="新增", operateInfo="新增文章 ${args[0]} 到(ID: ${args[1]}) 栏目下")
    void createArticle(Article article, Long channelId, String attachList, Long tempArticleId);

    @Logable(operateTable="文章", operateType="修改", operateInfo="修改文章 ${args[0]}")
    void updateArticle(Article article, Long channelId, String attachList);

    /**
     * 删除文章
     * 
     * @param articleId
     */
    @Logable(operateTable="文章", operateType="删除", operateInfo="删除了文章: ${returnVal} ")
    Article deleteArticle(Long articleId);

    /**
     * 获取文章
     * 
     * @param articleId
     * @return
     */
    Article getArticleById(Long articleId);

    /**
     * 移动文章
     * 
     * @param articleId
     * @param oldChannelId
     * @param channelId
     */
    @Logable(operateTable="文章", operateType="移动", operateInfo="将(ID: ${args[0]}) 文章，从(ID: ${args[1]}) 栏目下移动到(ID: ${args[2]}) 栏目下")
    void moveArticle(Long articleId, Long oldChannelId, Long channelId);

    /**
     * 获取栏目下属所有文章列表
     * 
     * @param channelId
     * @param pageNum
     * @param orderBy
     * @return
     */
    PageInfo getChannelArticles(Long channelId, Integer pageNum, String... orderBy);
    
    /**
     * <p>
     * 搜索栏目下的文章
     * </p>
     * 
     * @param condition
     * @return
     */
    Object[] searchArticleList(ArticleQueryCondition condition);

    /**
     * 文章上移/下移
     * 
     * @param articleId
     * @param toArticleId
     * @param channelId
     */
    @Logable(operateTable="文章", operateType="排序", operateInfo="(ID: ${args[0]}) 和 (ID: ${args[1]})文章顺序互相交换")
    void moveArticleDownOrUp(Long articleId, Long toArticleId, Long channelId);
 
    /**
     * 锁定文章
     * 
     * @param articleId
     */
    @Logable(operateTable="文章", operateType="锁定", operateInfo="锁定(ID: ${args[0]}) 文章")
    void lockingArticle(Long articleId);

    /**
     * 解锁文章
     * 
     * @param articleId
     */
    @Logable(operateTable="文章", operateType="解锁", operateInfo="解锁(ID: ${args[0]}) 文章")
    void unLockingArticle(Long articleId);
 
    /**
     * 置顶文章
     * 
     * @param articleId
     * @param channelId
     */
    @Logable(operateTable="文章", operateType="置顶", operateInfo="置顶文章(${returnVal})")
    Article doTopArticle(Long articleId);

    /**
     * 取消置顶
     * 
     * @param articleId
     * @param channelId
     */
    @Logable(operateTable="文章", operateType="解除置顶", operateInfo="取消置顶文章(${returnVal}) ")
    Article undoTopArticle(Long articleId);
 
}