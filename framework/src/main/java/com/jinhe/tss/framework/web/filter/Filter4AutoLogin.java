package com.jinhe.tss.framework.web.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.jinhe.tss.framework.exception.BusinessException;
import com.jinhe.tss.framework.exception.BusinessServletException;
import com.jinhe.tss.framework.exception.UserIdentificationException;
import com.jinhe.tss.framework.sso.ILoginCustomizer;
import com.jinhe.tss.framework.sso.IUserIdentifier;
import com.jinhe.tss.framework.sso.IdentityCard;
import com.jinhe.tss.framework.sso.LoginCustomizerFactory;
import com.jinhe.tss.framework.sso.UserIdentifierFactory;
import com.jinhe.tss.framework.sso.context.Context;
import com.jinhe.tss.framework.sso.context.RequestContext;
import com.jinhe.tss.framework.sso.identifier.AnonymousUserIdentifier;
import com.jinhe.tss.framework.sso.identifier.OnlineUserIdentifier;

/**
 * <p> 自动登录过滤器 </p>
 *
 * <pre>
 * 身份认证：<br/>
 *      检验用户身份是否合法，如果合法顺利通过；<br/>
 *      如果不合法，通过判断是否登录其他系统，使用单点登录方式自动登录系统，并顺利通过；<br/>
 *      如果不能使用单点登录方式登录，判断是否可以匿名访问，如果可以，使用匿名用户登录，并顺利通过；<br/>
 *      如果不能匿名访问或过程中出现错误信息，则抛出异常，返回客户端<br/>
 * 关于匿名访问：<br/>
 *      将匿名用户和普通用户完全一样处理<br/>
 * </pre>
 *
 */
@WebFilter(filterName = "AutoLoginFilter", 
		urlPatterns = {"/nothing"}, initParams = {
		@WebInitParam(name="ignoreServletPaths", value="/remote/OnlineUserService,/remote/LoginService,.in,js,htm,html,jpg,gif,css")
})
public class Filter4AutoLogin implements Filter {
	private static Logger log = Logger.getLogger(Filter4AutoLogin.class);

	private Set<String> ignoreServletPaths = new HashSet<String>();
 
	public void init(FilterConfig filterConfig) throws ServletException {
		String paths = filterConfig.getInitParameter("ignoreServletPaths");
		if (paths != null) {
			ignoreServletPaths.addAll(Arrays.asList(paths.split(",")));
		}
		log.info("AutoLoginFilter init! appCode=" + Context.getApplicationContext().getCurrentAppCode());
	}
 
	public void destroy() {
		ignoreServletPaths = null;
	}
 
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
            String servletPath = RequestContext.getServletPath((HttpServletRequest)request);
			for(String ignore : ignoreServletPaths) {
	            if(servletPath.toLowerCase().endsWith(ignore.toLowerCase())) {
	                chain.doFilter(request, response);
	                return;
	            }
	        }
            
			log.debug("current request path: " + servletPath);
		    IdentityCard card = authenticate();
		    /* 
		     * 初次登录或从其他系统SSO过来时，会新生成一个在本系统内通行的IdentityCard 。
		     * 身份认证通过，进入系统（身份认证合法以后，根据普通用户或匿名用户分别处理登录过程）。
		     */
		    if (card != null) {
		        /* 登录系统，初始化用户身份信息 */
	            Context.initIdentityInfo(card);
	            /* 登录自定义操作 */
	            customizerExcuteAfterLogin();
	            /* 保存Cookie信息到客户端 */
	            setCookie((HttpServletResponse)response, RequestContext.USER_TOKEN, card.getToken());
	        }
			
			chain.doFilter(request, response);
			
		} catch (UserIdentificationException e) {
			throw new BusinessServletException(e, true);
		} catch (Exception e) {
			throw new BusinessServletException(e);
		}
	}

    /**
     * 验证用户的登录信息，确认当前请求的用户是否已经已经登录（根据token和在线用户库判断），或者登录信息是否正确（userIdentifierClassName != null时，一般为初次登录）
     */
    private IdentityCard authenticate() throws UserIdentificationException {
        String currentAppCode = Context.getApplicationContext().getCurrentAppCode();
        
        IdentityCard card = null;
        
        /* 正常登录过程，正常登陆时在用户输入loginName请求系统返回userName的时同时返回的还有“用户对应的身份认证对象类名” */
        String userIdentifierClassName = Context.getRequestContext().getUserIdentifierClassName();
        if (userIdentifierClassName != null) {
        	card = validate(userIdentifierClassName);
        	log.debug(currentAppCode + "【登录模块】用户登录：" + card.getLoginName());
            return card;
        } 
        
    	// 已登录、超时（跳转）或者是匿名访问
    	String token = Context.getRequestContext().getUserToken();
    	if (token == null) {
    		// 匿名用户登录
    		card = validate(AnonymousUserIdentifier.class.getName());
    		log.debug(currentAppCode + "【登录模块】匿名用户登录");
            return card;
    	}  
    	
    	/* 如果请求中带的token和session中的token一致，说明此token已成功登陆过，pass直接访问 */
		if (token.equals(Context.getRequestContext().getAgoToken())) {
	        log.debug(currentAppCode + "【登录模块】已在线，直接访问");
        } 
		else {
            /* 
             * 如果请求中带的token和session中的token不一致，有两种可能：
             *  1、session中AgoToken为空（当应用跳转时（如CMS跳到TSS时）会出现）
             *  2、请求中的token是伪造的
             * 
             * 执行在线用户自动登录（用户已经登陆过，已经在在线用户库中注册）。。。。。。 
             */
			card = validate(OnlineUserIdentifier.class.getName());
			if (card != null) {
                log.debug(currentAppCode + "【登录模块】在线用户登录：" + card.getLoginName());
            } else {
                /* 如果token验证不通过（token没在在线用户库中注册过，过期了或伪造的），则采用匿名用户登录 */
				card = validate(AnonymousUserIdentifier.class.getName());
				log.debug(currentAppCode + "【登录模块】匿名用户登录");
			}
		}
		return card;
    }

	/**
	 * <p>
	 * 验证用户是否合法，包括正常登录、通过在线用户库单点登录、匿名用户登录等的身份验证。 <br/>
	 * 1、如果身份合法，返回身份证对象； <br/>
	 * 2、如果验证过程中产生需要重新登录的情况，都抛出UserIdentificationException异常 <br/>
	 * 3、如果用户Token在在线用户库里没注册，说明Token令牌是伪造的或是已经过期的，返回null，表示验证不通过（见OnlineUserIdentifier）。<br/>
	 * </p>
	 *
	 * @param userIdentifierClassName 身份验证器对象全类名
	 * @return
	 * @throws UserIdentificationException
	 */
	private IdentityCard validate(String userIdentifierClassName) throws UserIdentificationException {
		IUserIdentifier identifier = UserIdentifierFactory.instance().getUserIdentifier(userIdentifierClassName);
		return identifier.identify();
	}

	/**
	 * <p>
	 * 执行用户登录自定义类，只有用户登录时才执行
	 * </p>
	 */
	private void customizerExcuteAfterLogin() {
        ILoginCustomizer customizer = LoginCustomizerFactory.instance().getCustomizer();
		try {
            customizer.execute();
		} catch (Throwable e) {
			String customizerName = customizer.getClass().getName();
            String msg = "自定义登录操作（" + customizerName + "）失败，请先检查数据源配置是否正确。";
            throw new BusinessException(msg, e);
		}
	}

	/**
	 * <p>
	 * 设置需要返回的Cookie对象到response中
	 * </p>
	 * @param res   HttpServletResponse 返回对象
	 * @param name  String 名称
	 * @param value String 值
	 */
	private void setCookie(HttpServletResponse response, String name, String value) {
		HttpServletRequest req = Context.getRequestContext().getRequest();
		Cookie cookie = new Cookie(name, value);
		cookie.setPath(req.getContextPath());
		cookie.setMaxAge(-1);
		cookie.setSecure(req.isSecure());
        response.addCookie(cookie);
	}
}
