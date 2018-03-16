package com.kfit.config.shiro;

import com.google.code.kaptcha.Constants;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.springframework.http.HttpRequest;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * 自定义过滤器 在此过滤器进行验证码匹配
 *
 */
public class CustomFormAuthenticationFilter extends FormAuthenticationFilter {
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        System.out.println("----------------------------------------------------------");
        if (isLoginSubmission(request,response)){//是登录请求提交
            System.out.println("----------------------------------------------------------");
            String received = request.getParameter("kaptchaValidate");

            if(received != null){
                HttpServletRequest request1 = (HttpServletRequest) request;

                String captcha = (String) request1.getSession().getAttribute(Constants.KAPTCHA_SESSION_KEY);

                boolean b = received.equalsIgnoreCase(captcha);
                if (!b){
                    System.out.println("------->>>>----------");
                    request.setAttribute("shiroLoginFailure","kaptchaValidateFailed");
                    return  true;
                }
            }

        }
        return super.onAccessDenied(request, response);
    }
}
