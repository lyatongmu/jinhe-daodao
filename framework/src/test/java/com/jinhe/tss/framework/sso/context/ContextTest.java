package com.jinhe.tss.framework.sso.context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import com.jinhe.tss.framework.sso.AnonymousOperator;
import com.jinhe.tss.framework.sso.IdentityCard;
 
public class ContextTest extends TestCase {

	private IMocksControl mocksControl;

	private HttpServletRequest request;

	private HttpSession session;
 
	protected void setUp() throws Exception {
	    mocksControl =  EasyMock.createControl();
	    request = mocksControl.createMock(HttpServletRequest.class);
		session = mocksControl.createMock(HttpSession.class);
		super.setUp();
	}

	/**
	 * Test method for
	 * {@link com.jinhe.tss.core.sso.context.Context#getRequestContext()}.
	 */
	public final void testGetRequestContext4MultiThread() {
	    EasyMock.expect(request.getSession()).andReturn(session).times(0, 8);
        EasyMock.expect(request.getHeader(RequestContext.USER_CLIENT_IP)).andReturn("127.0.0.1").times(0, 3);
        EasyMock.expect(request.getContextPath()).andReturn("/tss");
        EasyMock.expect(request.getRequestURI()).andReturn("/tss/login.do");
        
        EasyMock.expect(session.getId()).andReturn("1").times(0, 3);
        EasyMock.expect(session.getAttribute(RequestContext.USER_TOKEN)).andReturn("token").times(0, 3);
        IdentityCard identityCard = new IdentityCard("token", AnonymousOperator.anonymous);
        EasyMock.expect(session.getAttribute(RequestContext.IDENTITY_CARD)).andReturn(identityCard).times(0, 3);
        
        EasyMock.replay(request); // 让mock 准备重放记录的数据
//        EasyMock.replay(session);

		Context.initRequestContext(request);
		Thread t = new ContextSupportThread() {
			public void runSupportContext() {
				TestCase.assertNotNull(Context.getToken());
			}
		};
		t.start();
	}

}