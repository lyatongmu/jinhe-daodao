package com.jinhe.tss.um.servlet;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.jinhe.tss.framework.sso.SSOConstants;
import com.jinhe.tss.um.TxSupportTest4UM;

public class GetLoginInfoServletTest extends TxSupportTest4UM {
    
    public void testDoPost() {
        MockHttpServletRequest request = new MockHttpServletRequest(); 
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        request.addParameter(SSOConstants.LOGINNAME_IN_SESSION, "Admin");
        
        try {
            new GetLoginInfoServlet().doPost(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
