package com.jinhe.tss.framework.sso.identity.identifier;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import com.jinhe.tss.framework.exception.UserIdentificationException;
import com.jinhe.tss.framework.sso.AnonymousOperator;
import com.jinhe.tss.framework.sso.IUserIdentifier;
import com.jinhe.tss.framework.sso.IdentityCard;
import com.jinhe.tss.framework.sso.appserver.AppServer;
import com.jinhe.tss.framework.sso.context.Context;
import com.jinhe.tss.framework.sso.context.RequestContext;
import com.jinhe.tss.framework.sso.identifier.AnonymousUserIdentifier;
import com.jinhe.tss.framework.sso.identity.MockAppServerStorer;
import com.jinhe.tss.framework.sso.identity.MockApplicationContext;
import com.jinhe.tss.framework.sso.identity.MockOnlineUserManagerFactory;
import com.jinhe.tss.framework.sso.online.OnlineUserManagerFactory;
 
public class AnonymousUserIdentifierTest extends TestCase {
    
    private IUserIdentifier identifier;

    private MockHttpServletRequest request;
 
    protected void setUp() throws Exception {
        super.setUp();
        MockOnlineUserManagerFactory.init();
        
        AppServer server = new AppServer();
        server.setCode("Core");
        server.setName("Core");
        server.setUserDepositoryCode("tss");
        MockAppServerStorer storer = new MockAppServerStorer();
        storer.putAppServer("Core", server);
        
        Context.initApplicationContext(new MockApplicationContext(storer, "Core"));
        
        request = new MockHttpServletRequest();
        request.setCharacterEncoding("GBK");
        request.setSession(new MockHttpSession());
        
        Context.initRequestContext(request);
        
        identifier = new AnonymousUserIdentifier();
    }
 
    protected void tearDown() throws Exception {
        super.tearDown();
        identifier = null;
        request = null;
    }

    /** Test method for {@link com.jinhe.tss.core.sso.identifier.BaseUserIdentifier#identify()}. */
    public final void testIdentify4Ok() {
        request.addHeader(RequestContext.ANONYMOUS_REQUEST, "true"); // 允许匿名访问
        IdentityCard card = null;
        try {
            card = identifier.identify();
        } catch (UserIdentificationException e) {
            fail("UserIdentificationException");
        }
        assertNotNull(card);
        assertEquals(AnonymousOperator.anonymous.getUserName(), card.getUserName());
        assertTrue(OnlineUserManagerFactory.getManager().isOnline(card.getToken()));
    }

    /** Test method for {@link com.jinhe.tss.core.sso.identifier.BaseUserIdentifier#identify()}. */
    public final void testIdentify4Fail() {
        IdentityCard card = null;
        try {
            card = identifier.identify();
            fail("应该发生异常，但没有发生");
        } catch (UserIdentificationException e) {
            assertTrue(true);
        }
        assertNull(card);
    }
}
