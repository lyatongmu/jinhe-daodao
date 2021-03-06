package com.jinhe.tss.framework.sso.identity;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import com.jinhe.tss.framework.sso.AnonymousOperator;
import com.jinhe.tss.framework.sso.DemoOperator;
import com.jinhe.tss.framework.sso.DemoUserIdentifier;
import com.jinhe.tss.framework.sso.PWDOperator;
import com.jinhe.tss.framework.sso.Environment;
import com.jinhe.tss.framework.sso.IOperator;
import com.jinhe.tss.framework.sso.IdentityCard;
import com.jinhe.tss.framework.sso.TokenUtil;
import com.jinhe.tss.framework.sso.context.Context;
import com.jinhe.tss.framework.sso.context.RequestContext;
import com.jinhe.tss.framework.sso.online.OnlineUserManagerFactory;
import com.jinhe.tss.framework.web.filter.Filter4AutoLogin;
 
public class AutoLoginFilterTest extends TestCase {

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private MockHttpSession session;

    private FilterChain chain;

    private Filter4AutoLogin filter;

    protected void setUp() throws Exception {
        super.setUp();
        
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        session = new MockHttpSession();
        
        request.setSession(session);
        request.addHeader(RequestContext.USER_CLIENT_IP, "192.168.0.12");
        request.setCharacterEncoding("GBK");
        
        Context.initApplicationContext(MockApplicationContext.getDefaultApplicationContext());
       
        filter = new Filter4AutoLogin();
        chain = new MockFilterChain();
    }
 
    protected void tearDown() throws Exception {
        super.tearDown();
        request = null;
        response = null;
        session = null;
    }
 
    public final void testDoFilter4Pass() throws IOException, ServletException {
        String token = "token";
        session.setAttribute(RequestContext.USER_TOKEN, token);
        
        Cookie[] cookies = new Cookie[] { new Cookie(RequestContext.USER_TOKEN, token) };
        request.setCookies(cookies);
        
        PWDOperator testPasswordOperator = new PWDOperator(new Long(1));
        IdentityCard card = new IdentityCard(token, testPasswordOperator);
        session.setAttribute(RequestContext.IDENTITY_CARD, card);
        
        Context.initRequestContext(request);
        Context.initIdentityInfo(card);
        
        filter.doFilter(request, response, chain);
        assertNotNull(Context.getRequestContext().getIdentityCard());
        assertEquals(testPasswordOperator.getLoginName(), Environment.getOperatorName());
        assertEquals(MockFilterChain.RESPONSE_BODY_STRING, response.getContentAsString());
    }
 
    public final void testDoFilter4AnonymousLogin() throws IOException, ServletException {
        request.addHeader(RequestContext.ANONYMOUS_REQUEST, "true"); // 允许匿名
        Context.initRequestContext(request);
        filter.doFilter(request, response, chain);

        assertNotNull(Context.getRequestContext().getIdentityCard());
        assertEquals(AnonymousOperator.anonymous.getLoginName(), Environment.getOperatorName());
        assertEquals(MockFilterChain.RESPONSE_BODY_STRING, response.getContentAsString());
    }
 
    public final void testDoFilter4AnonymousException() throws IOException, ServletException {
        Context.initRequestContext(request);
        try {
            filter.doFilter(request, response, chain);
            fail("应该发生异常而没有发生");
        } catch (Exception e) {
            assertTrue(true);
        }
    }
 
    public final void testDoFilter4OnlineException2Anonymous() throws IOException, ServletException {
        request.addHeader(RequestContext.ANONYMOUS_REQUEST, "true");
        
        String token = "token";
        Cookie[] cookies = new Cookie[] { new Cookie(RequestContext.USER_TOKEN, token) };
        request.setCookies(cookies);
        
        Context.initRequestContext(request);
        filter.doFilter(request, response, chain);
        
        assertEquals(AnonymousOperator.anonymous.getLoginName(), Environment.getOperatorName());
        assertEquals(MockFilterChain.RESPONSE_BODY_STRING, response.getContentAsString());
    }
 
    public final void testDoFilter4OnlineException2AnonymousException() throws IOException, ServletException {
        String token = "token";
        Cookie[] cookies = new Cookie[] { new Cookie(RequestContext.USER_TOKEN, token) };
        request.setCookies(cookies);
        Context.initRequestContext(request);
        try {
            filter.doFilter(request, response, chain);
            fail("应该发生异常而没有发生");
        } catch (Exception e) {
            assertTrue(true);
        }
    }
 
    public final void testDoFilter4OnlineOk() throws IOException, ServletException {
        request.addHeader(RequestContext.ANONYMOUS_REQUEST, "true");
        
        IOperator operator = new DemoOperator(new Long(2));
        String token = TokenUtil.createToken("sessionId", operator.getId());
        Cookie[] cookies = new Cookie[] { new Cookie(RequestContext.USER_TOKEN, token) };
        request.setCookies(cookies);
        
        Context.initRequestContext(request);
        
        MockOnlineUserManagerFactory.init();
        MockIdentityTranslatorFactory.init();
        
        OnlineUserManagerFactory.getManager().register(token, "appCode", "sessionId", new Long(3), null);
        
        filter.doFilter(request, response, chain);
        
        assertNotNull(Context.getToken());
        assertEquals(new Long(operator.getId().longValue() + 1), Environment.getOperatorId());
        assertEquals(MockFilterChain.RESPONSE_BODY_STRING, response.getContentAsString());
    }
 
    public final void testDoFilter4LoginException() {
        String token = "token";
        Cookie[] cookies = new Cookie[] { new Cookie(RequestContext.USER_TOKEN, token) };
        request.setCookies(cookies);
        request.addHeader("identifier", MockUserIdentifier.class.getName());
        Context.initRequestContext(request);
        try {
            filter.doFilter(request, response, chain);
            fail("应该发生异常而没有发生");
        } catch (Exception e) {
            assertTrue(true);
        }
    }
 
    public final void testDoFilter4LoginOk() throws IOException, ServletException {
        request.addHeader("identifier", DemoUserIdentifier.class.getName());
        Context.initRequestContext(request);
        filter.doFilter(request, response, chain);

        assertNotNull(Context.getRequestContext().getIdentityCard());
        assertEquals(AnonymousOperator.anonymous.getLoginName(), Environment.getOperatorName());
        assertEquals(MockFilterChain.RESPONSE_BODY_STRING, response.getContentAsString());
    }
}
