package com.kfit.root.controller;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.kfit.core.bean.UserInfo;

@Controller
public class HomeController {

	@Autowired
	private Producer producer;
	@RequestMapping(value={"/","/index"})
	public String index(Map<String,Object> map){
		//获取到用户信息;
		Subject subject  = SecurityUtils.getSubject();
		UserInfo ui = (UserInfo) subject.getPrincipal();
		map.put("userInfo",ui);
		return "/index";
	}

	
	@RequestMapping(value="/login",method=RequestMethod.GET)
	public String login(HttpServletRequest request){
//		SavedRequest savedRequest = WebUtils.getSavedRequest(request);
//		if(savedRequest != null){
//			System.out.println(savedRequest.getMethod()+"==="+savedRequest.getRequestURI()+"---"+savedRequest.getRequestUrl());
//		}
		return "login";
	}
	
	// 登录提交地址和applicationontext-shiro.xml配置的loginurl一致。 (配置文件方式的说法)
	@RequestMapping(value="/login",method=RequestMethod.POST)
	public String login(HttpServletRequest request, Map<String, Object> map) throws Exception {
		System.out.println("HomeController.login()");
		// 登录失败从request中获取shiro处理的异常信息。
		// shiroLoginFailure:就是shiro异常类的全类名.
		String exception = (String) request.getAttribute("shiroLoginFailure");

		System.out.println("exception=" + exception);
		String msg = "";
		if (exception != null) {
			if (UnknownAccountException.class.getName().equals(exception)) {
				System.out.println("UnknownAccountException -- > 账号不存在：");
				msg = "UnknownAccountException -- > 账号不存在：";
			} else if (IncorrectCredentialsException.class.getName().equals(exception)) {
				System.out.println("IncorrectCredentialsException -- > 密码不正确：");
				msg = "IncorrectCredentialsException -- > 密码不正确：";
			} else if ("kaptchaValidateFailed".equals(exception)) {
				System.out.println("kaptchaValidateFailed -- > 验证码错误");
				msg = "kaptchaValidateFailed -- > 验证码错误";
			} else if("org.apache.shiro.authc.ExcessiveAttemptsException".equals(exception)){
				msg = "ExcessiveAttemptsException -- > 密码输错次数过多";
			}else{
				msg = "else >> "+exception;
				System.out.println("else -- >" + exception);
			}
		}
		map.put("msg", msg);
		// 此方法不处理登录成功,由shiro进行处理.
		return "/login";
	}
	@RequestMapping("/captcha")
	public void captcha(HttpServletRequest request, HttpServletResponse rsp){

		rsp.setDateHeader("Expires", 0);//失效时间
		rsp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");//缓存设置
		rsp.addHeader("Cache-Control", "post-check=0, pre-check=0");
		rsp.setHeader("Pragma", "no-cache");
		rsp.setContentType("image/jpeg");//设置返回类型

		HttpSession session = request.getSession();
		//获得验证码 字符
		String captcha = producer.createText();

		session.setAttribute(Constants.KAPTCHA_SESSION_KEY,captcha);
		ServletOutputStream out = null;

		try {
			out = rsp.getOutputStream();

			BufferedImage image = producer.createImage(captcha);

			ImageIO.write(image,"jpg",out);

		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			//关闭流
			if (out != null){
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

	}
	
}
