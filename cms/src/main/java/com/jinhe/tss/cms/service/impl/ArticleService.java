package com.jinhe.tss.cms.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.jinhe.tss.cms.CMSConstants;
import com.jinhe.tss.cms.dao.IArticleDao;
import com.jinhe.tss.cms.dao.IChannelDao;
import com.jinhe.tss.cms.entity.Article;
import com.jinhe.tss.cms.entity.Attachment;
import com.jinhe.tss.cms.entity.Channel;
import com.jinhe.tss.cms.helper.ArticleHelper;
import com.jinhe.tss.cms.helper.ArticleQueryCondition;
import com.jinhe.tss.cms.service.IArticleService;
import com.jinhe.tss.framework.exception.BusinessException;
import com.jinhe.tss.framework.persistence.pagequery.PageInfo;
import com.jinhe.tss.framework.sso.Environment;
import com.jinhe.tss.util.BeanUtil;
import com.jinhe.tss.util.EasyUtils;
 
public class ArticleService implements IArticleService {
    
    protected final Logger log = Logger.getLogger(getClass());

	@Autowired private IArticleDao articleDao;
	@Autowired private IChannelDao channelDao;
 
    public Article getArticleById(Long articleId) {
        Article article = getArticleOnly(articleId);
        
        Map<String, Attachment> attachments = articleDao.getArticleAttachments(articleId);
        article.getAttachments().putAll(attachments);
        return article;
    }
    
	public void createArticle(Article article, Long channelId, String attachList, Long tempArticleId) {
		Channel channel = channelDao.getEntity(channelId);
		article.setChannel(channel);
		article.setSeqNo(articleDao.getChannelArticleNextOrder(channelId));
 
		// set over date
		Date calculateOverDate = ArticleHelper.calculateOverDate(article, channel);
        if( calculateOverDate != null ) {
			article.setOverdueDate(calculateOverDate);
        }
		articleDao.saveArticle(article);
        
		// 处理附件
        List<String> attachSeqNos = new LinkedList<String>();
		if ( !EasyUtils.isNullOrEmpty(attachList) ) {			
            StringTokenizer st = new StringTokenizer(attachList, ",");
            while (st.hasMoreTokens()) {
                attachSeqNos.add(st.nextToken());
            }
        }
		
        Map<String, Attachment> attachments = articleDao.getArticleAttachments(tempArticleId); // 后台查找的新建文章时上传的附件列表
        for ( Attachment attachment : attachments.values() ) {
			Integer seqNo = attachment.getSeqNo();
            if (attachSeqNos.contains(seqNo.toString())) { // 判断附件在文章保存时是否还存在
				translatePath(attachment, article, channelId);
				
				// 新增附件信息
				Attachment attach = new Attachment();
				BeanUtil.copy(attach, attachment);
				attach.setArticle(article);
				attach.setSeqNo(seqNo);
				
				articleDao.createObject(attach);
			}
            else {
				// 删除附件
				String[] uploadName = ArticleHelper.getAttachUploadPath(channel.getSite(), attachment);
                new File(uploadName[0]).delete();
			}
            //删除老的附件信息
			articleDao.delete(attachment);
		}
	}
	
	// 栏目文章关系
    public void createChannelArticleRelation(Long channelId, Long articleId) {
        Article article = getArticleOnly(articleId);
        Channel channel = channelDao.getEntity(channelId);
        article.setChannel(channel);
        article.setSeqNo(articleDao.getChannelArticleNextOrder(channelId));
        
        articleDao.saveArticle(article);
    }

	/**
	 * 将文章内容中的临时地址替换成真实地址。
     * 主要是将临时生成的附件ID替换成附件所属文章的ID。
	 */
	private void translatePath(Attachment attachment, Article article, Long channelId) {
		// download.fun?id=1216&seqNo=1
		String relateDownloadUrl = attachment.getRelateDownloadUrl(); 
        relateDownloadUrl = relateDownloadUrl.replaceAll("&", "&amp;"); //将&替换成&amp;
        
		StringBuffer sb = new StringBuffer(article.getContent());
		int index = sb.indexOf(relateDownloadUrl);
		while (index != -1) {
			StringBuffer buffer = new StringBuffer();
			int idIndex = relateDownloadUrl.indexOf("?id=");
			String realAttUploadName;
			if (idIndex != -1) {
				realAttUploadName = relateDownloadUrl.substring(0, idIndex + 4);
				realAttUploadName += article.getId() + relateDownloadUrl.substring(relateDownloadUrl.indexOf("&amp;"));
				buffer.append(sb.substring(0, index)).append(realAttUploadName).append(sb.substring(index + relateDownloadUrl.length()));
				sb = buffer;
			}
			index = sb.indexOf(relateDownloadUrl);
		}
		article.setContent(sb.toString());
	}
    
    public void updateArticle(Article article, Long channelId, String attachList) {
    	
        articleDao.saveArticle(article);
        
        // 处理附件, attachList为需要删除的附件列表
        if ( !EasyUtils.isNullOrEmpty(attachList) ) {
            StringTokenizer st = new StringTokenizer(attachList, ",");
            List<String> attachSeqNos = new LinkedList<String>();
            while (st.hasMoreTokens()) {
                attachSeqNos.add(st.nextToken());
            }
            
            Map<String, Attachment> attachments = articleDao.getArticleAttachments(article.getId());
            for ( Attachment attachment : attachments.values() ) {
                if (!attachSeqNos.contains(attachment.getSeqNo().toString()))
                   continue;
                
                // 删除附件
                articleDao.delete(attachment);
                Channel site = channelDao.getSiteByChannel(channelId);
                String path = site.getPath() + "/" + site.getAttanchmentPath(attachment) + "/" + attachment.getLocalPath();
                new File(path).delete();
            }
        }
    }

    public Article deleteArticle(Long articleId) {
        Article article = getArticleOnly(articleId);
        articleDao.deleteArticle(article);
        
        return article;
	}
    
    private Article getArticleOnly(Long articleId){
        Article article = articleDao.getEntity(articleId);
        if (article == null) {
            throw new NullPointerException("未找到文章！");
        }
        
        return article;
    }
 
    public void moveArticle(Long articleId, Long oldChannelId, Long channelId) {
        Article article = articleDao.getEntity(articleId);
        Channel channel = channelDao.getEntity(channelId);
        article.setChannel(channel);
        
        articleDao.update(article);
	}

    public void moveArticleDownOrUp(Long articleId, Long toArticleId, Long channelId) {
        Article article = articleDao.getEntity(articleId);
        Article toArticle = articleDao.getEntity(toArticleId);

        article.setSeqNo(toArticle.getSeqNo());
        toArticle.setSeqNo(article.getSeqNo());

		articleDao.update(article);
		articleDao.update(toArticle);
	}

    public void lockingArticle(Long articleId) {
		Article article = getArticleOnly(articleId);
        if (!CMSConstants.START_STATUS.equals(article.getStatus())) {
			throw new BusinessException("文章不处于初始状态，不能锁定");
        }
		
		article.setStatus(CMSConstants.LOCKING_STATUS);
		articleDao.update(article);
	}

	public void unLockingArticle(Long articleId) {
        Article article = getArticleOnly(articleId);
		if ( !CMSConstants.LOCKING_STATUS.equals(article.getStatus()) ) {
			throw new BusinessException("文章不处于锁定状态，不能解除锁定");
		}
		
		article.setStatus(CMSConstants.START_STATUS);
		articleDao.update(article);
	}
 
	public Article doTopArticle(Long articleId) {
	    Article article = getArticleOnly(articleId);
	    article.setIsTop(CMSConstants.TRUE);
		articleDao.update(article);
		
		return article;
	}
 
	public Article undoTopArticle(Long articleId) {
	    Article article = getArticleOnly(articleId);
	    article.setIsTop(CMSConstants.FALSE);
        articleDao.update(article);
        
        return article;
	}
 
    public PageInfo getChannelArticles(Long channelId, Integer pageNum, String...orderBy) {
       if( !channelDao.checkBrowsePermission(channelId) ) {
            log.error("用户【" + Environment.getOperatorName() + "】试图访问没有文章浏览权限的栏目【" + channelId + "】");
            return new PageInfo();
        }
        return articleDao.getPageList(channelId, pageNum, orderBy);
    }

	public Object[] searchArticleList(ArticleQueryCondition condition) {
		// 将用户有浏览权限的选定栏目下子栏目ID列表存入临时表中
		Long channelId = condition.getChannelId();
        List<Channel> children = channelDao.getChildrenById(channelId, CMSConstants.OPERATION_VIEW);
		channelDao.insertEntityIds2TempTable(children);
		condition.setChannelId(null);
 
		PageInfo page = articleDao.getSearchArticlePageList(condition);
        List<?> list = page.getItems();
        
		List<Article> articleList = new ArrayList<Article>();
		if ( !EasyUtils.isNullOrEmpty(list) ) {
			for ( Object temp : list ) {
                Article article = ArticleHelper.createArticle((Object[]) temp);
				articleList.add(article);
			}
		}
		return new Object[]{articleList, page};
	}
}