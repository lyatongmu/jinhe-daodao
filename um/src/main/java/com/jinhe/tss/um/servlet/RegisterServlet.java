package com.jinhe.tss.um.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jinhe.tss.framework.Global;
import com.jinhe.tss.framework.web.dispaly.SuccessMessageEncoder;
import com.jinhe.tss.framework.web.dispaly.XmlPrintWriter;
import com.jinhe.tss.um.UMConstants;
import com.jinhe.tss.um.entity.User;
import com.jinhe.tss.um.service.IUserService;
import com.jinhe.tss.um.sso.UMSLocalUserPWDIdentifier;
import com.jinhe.tss.util.DateUtil;

/**
 * <p> 用户注册Servlet </p>
 * <p>
 * 因普通的Action会被要求登录用户才能访问，所以这里采用Servlet来实现注册功能。
 * </p>
 */
@WebServlet(urlPatterns="/register.in")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = -740569423483772472L;
    
    private IUserService service;
 
	public void init() {
		service = (IUserService) Global.getContext().getBean("UserService");
	}
	
	public void destroy() {
		service = null;
	}
	
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
 
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = new User();
        // 默认初始值
        user.setApplicationId(UMConstants.TSS_APPLICATION_ID);
        // 设置默认认证方式为UMS本地认证
        user.setAuthenticateMethod(UMSLocalUserPWDIdentifier.class.getName());
        // 前台传递的参数
        user.setLoginName(request.getParameter("loginName"));
        user.setPassword(request.getParameter("password"));
        user.setUserName(request.getParameter("userName"));
        user.setEmployeeNo(request.getParameter("employeeNo"));
        user.setSex(request.getParameter("sex"));
        user.setMail(request.getParameter("mail"));
        String birthday = request.getParameter("birthday");
        user.setAddress(request.getParameter("address"));
        user.setTelephone(request.getParameter("telephone"));
        user.setPostalCode(request.getParameter("postalCode"));
        user.setPasswordQuestion(request.getParameter("passwordQuestion"));
        user.setPasswordAnswer(request.getParameter("passwordAnswer"));
        user.setCertificateCategory(request.getParameter("certificateCategory"));
        user.setCertificateNumber(request.getParameter("certificateNumber"));
       
        user.setBirthday(DateUtil.parse(birthday));

        service.registerUser(user);

        response.setContentType("text/html;charset=GBK");
        SuccessMessageEncoder encoder = new SuccessMessageEncoder("用户注册成功！", "用户注册成功！");
        encoder.print(new XmlPrintWriter(response.getWriter()));
    }
}
