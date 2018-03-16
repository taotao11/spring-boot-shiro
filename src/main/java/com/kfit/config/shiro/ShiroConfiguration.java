package com.kfit.config.shiro;

import java.util.*;

import javax.servlet.Filter;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring4.SpringTemplateEngine;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;

/**
 * （1）Apache Shiro 核心通过 Filter 来实现，就好像SpringMvc 通过DispachServlet 来主控制一样。 
 * （2）既然是使用 Filter 一般也就能猜到，是通过URL规则来进行过滤和权限校验，所以我们需要定义一系列关于URL的规则和访问权限。
 * 
 * @author Angel -- 守护天使
 * @version v.0.1
 * @date 2017年10月13日
 */
@Configuration
public class ShiroConfiguration {
	/**
	 * ShiroFilterFactoryBean 处理拦截资源文件问题。
	 * 注意：单独一个ShiroFilterFactoryBean配置是或报错的，以为在
	 * 初始化ShiroFilterFactoryBean的时候需要注入：SecurityManager
	 * 
	 	Filter Chain定义说明 
		1、一个URL可以配置多个Filter，使用逗号分隔 
		2、当设置多个过滤器时，全部验证通过，才视为通过 
		3、部分过滤器可指定参数，如perms，roles
	 * 
	 */
	@Bean
	public ShiroFilterFactoryBean shirFilter(SecurityManager securityManager){
		System.out.println("ShiroConfiguration.shirFilter()");
		ShiroFilterFactoryBean shiroFilterFactoryBean  = new ShiroFilterFactoryBean();
		
		 // 必须设置 SecurityManager  
		shiroFilterFactoryBean.setSecurityManager(securityManager);
		//自定义验证码 过滤器
		Map<String,Filter> filtersMap = new HashMap<String,Filter>();
		CustomFormAuthenticationFilter customFormAuthenticationFilter = new CustomFormAuthenticationFilter();
		filtersMap.put("authc",customFormAuthenticationFilter);
		shiroFilterFactoryBean.setFilters(filtersMap);

//	      //自定义filter.
//        Map<String,Filter> filters  = new HashMap<String,Filter>();
//        MyFormAuthenticationFilter formAuthenticationFilter = new MyFormAuthenticationFilter();
//        filters.put("authc", formAuthenticationFilter);//重新定义formAuthenticationFilter
//		shiroFilterFactoryBean.setFilters(filters);//交给spring shiroFilter管理

		
		//拦截器.
		Map<String,String> filterChainDefinitionMap = new LinkedHashMap<String,String>();
		
		//配置退出 过滤器,其中的具体的退出代码Shiro已经替我们实现了
		filterChainDefinitionMap.put("/logout", "logout");
		
		//设置favicon.ico:匿名访问anon
		filterChainDefinitionMap.put("/favicon.ico", "anon");
		filterChainDefinitionMap.put("/js/**", "anon");
		filterChainDefinitionMap.put("/css/**", "anon");
		filterChainDefinitionMap.put("/img/**", "anon");
		filterChainDefinitionMap.put("/captcha", "anon");
		//filterChainDefinitionMap.put("/login", "authc");
		
		//配置记住我或认证通过可以访问的地址
        filterChainDefinitionMap.put("/index", "user");
        filterChainDefinitionMap.put("/", "user");

		
		
		//<!-- 过滤链定义，从上向下顺序执行，一般将 /**放在最为下边 -->:这是一个坑呢，一不小心代码就不好使了;
	    //<!-- authc:所有url都必须认证通过才可以访问; anon:所有url都都可以匿名访问-->
		filterChainDefinitionMap.put("/**", "authc");
		
		// 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
        shiroFilterFactoryBean.setLoginUrl("/login");
        // 登录成功后要跳转的链接
        shiroFilterFactoryBean.setSuccessUrl("/index");
        //未授权界面;
        shiroFilterFactoryBean.setUnauthorizedUrl("/403");
		
		shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
		return shiroFilterFactoryBean;
	}
	
	
	@Bean
	public SecurityManager securityManager(){
		DefaultWebSecurityManager securityManager =  new DefaultWebSecurityManager();
		securityManager.setRealm(myShiroRealm());
		//注入缓存管理器;
		securityManager.setCacheManager(ehCacheManager());//这个如果执行多次，也是同样的一个对象;
		//注入记住我管理器;
		securityManager.setRememberMeManager(rememberMeManager());
				
		return securityManager;
	}
	
	
	/**
	 * 身份认证realm;
	 * (这个需要自己写，账号密码校验；权限等)
	 * @return
	 */
	@Bean
	public MyShiroRealm myShiroRealm(){
		MyShiroRealm myShiroRealm = new MyShiroRealm();
		myShiroRealm.setCredentialsMatcher(hashedCredentialsMatcher());
		return myShiroRealm;
	}
	
	/**
	 * 凭证匹配器
	 * （由于我们的密码校验交给Shiro的SimpleAuthenticationInfo进行处理了
	 *  所以我们需要修改下doGetAuthenticationInfo中的代码;
	 * ）
	 * @return
	 */
	@Bean
	public HashedCredentialsMatcher hashedCredentialsMatcher(){
//		HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
		//自定义密码匹配器
		MyHashedCredentialsMatcher hashedCredentialsMatcher = new MyHashedCredentialsMatcher(ehCacheManager());
		hashedCredentialsMatcher.setHashAlgorithmName("md5");//散列算法:这里使用MD5算法;
		hashedCredentialsMatcher.setHashIterations(2);//散列的次数，比如散列两次，相当于 md5(md5(""));
		return hashedCredentialsMatcher;
	}
	
	
	/**
	 *  开启shiro aop注解支持.
	 *  使用代理方式;所以需要开启代码支持;
	 * @param securityManager
	 * @return
	 */
	@Bean
	public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager){
		AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
		authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
		return authorizationAttributeSourceAdvisor;
	}
	
	/**
	 * shiro缓存管理器;
	 * 需要注入对应的其它的实体类中：
	 * 1、安全管理器：securityManager
	 * 可见securityManager是整个shiro的核心；
	 * @return
	 */
	@Bean
	public EhCacheManager ehCacheManager(){
		System.out.println("ShiroConfiguration.getEhCacheManager()");
		EhCacheManager cacheManager = new EhCacheManager();
		cacheManager.setCacheManagerConfigFile("classpath:config/ehcache-shiro.xml");
		return cacheManager;
	}
	
	
	/**
	 * cookie对象;
	 * @return
	 */
	@Bean
	public SimpleCookie rememberMeCookie(){
		System.out.println("ShiroConfiguration.rememberMeCookie()");
		//这个参数是cookie的名称，对应前端的checkbox 的name = rememberMe
		SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
		//<!-- 记住我cookie生效时间30天 ,单位秒;-->
		simpleCookie.setMaxAge(259200);
		return simpleCookie;
	}
	
	/**
	 * cookie管理对象;
	 * @return
	 */
	@Bean
	public CookieRememberMeManager rememberMeManager(){
		System.out.println("ShiroConfiguration.rememberMeManager()");
		CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
		cookieRememberMeManager.setCookie(rememberMeCookie());
		return cookieRememberMeManager;
	}

	/**
	 * shiro thymeleaf 解析
	 * @return
	 */
	@Bean
    public ShiroDialect shiroDialect() {
        return new ShiroDialect();
    }

	/**
	 * 验证码生成器;
	 * @return
	 */
	@Bean
	public Producer producer(){
		System.out.println("ShiroConfiguration.captchaProducer()");
		DefaultKaptcha producer = new DefaultKaptcha();
		Properties properties = new Properties();

		/*
kaptcha.border  是否有边框  默认为true  我们可以自己设置yes，no
kaptcha.border.color   边框颜色   默认为Color.BLACK
kaptcha.border.thickness  边框粗细度  默认为1
kaptcha.producer.impl   验证码生成器  默认为DefaultKaptcha
kaptcha.textproducer.impl   验证码文本生成器  默认为DefaultTextCreator
kaptcha.textproducer.char.string   验证码文本字符内容范围  默认为abcde2345678gfynmnpwx
kaptcha.textproducer.char.length   验证码文本字符长度  默认为5
kaptcha.textproducer.font.names    验证码文本字体样式  默认为new Font("Arial", 1, fontSize), new Font("Courier", 1, fontSize)
kaptcha.textproducer.font.size   验证码文本字符大小  默认为40
kaptcha.textproducer.font.color  验证码文本字符颜色  默认为Color.BLACK
kaptcha.textproducer.char.space  验证码文本字符间距  默认为2
kaptcha.noise.impl    验证码噪点生成对象  默认为DefaultNoise
kaptcha.noise.color   验证码噪点颜色   默认为Color.BLACK
kaptcha.obscurificator.impl   验证码样式引擎  默认为WaterRipple
kaptcha.word.impl   验证码文本字符渲染   默认为DefaultWordRenderer
kaptcha.background.impl   验证码背景生成器   默认为DefaultBackground
kaptcha.background.clear.from   验证码背景颜色渐进   默认为Color.LIGHT_GRAY
kaptcha.background.clear.to   验证码背景颜色渐进   默认为Color.WHITE
kaptcha.image.width   验证码图片宽度  默认为200
kaptcha.image.height  验证码图片高度  默认为50
		 */

		// 是否有边框  默认为true  我们可以自己设置yes，no
		properties.put("kaptcha.border","yes");

		//边框颜色   默认为Color.BLACK
		properties.put("kaptcha.border.color","105,179,90");

		//字体颜色;
		properties.put("kaptcha.textproducer.font.color","blue");

		//验证码样式引擎  默认为WaterRipple
		properties.put("kaptcha.obscurificator.impl","com.google.code.kaptcha.impl.ShadowGimpy");

		// 验证码图片宽度  默认为200
		properties.put("kaptcha.image.width","145");
		//验证码图片高度  默认为50
		properties.put("kaptcha.image.height","45");

		//验证码文本字符大小  默认为40
		properties.put("kaptcha.textproducer.font.size","45");

		//存放在session中的key;
		properties.put("kaptcha.session.key","code");

		//产生字符的长度
		properties.put("kaptcha.textproducer.char.length","4");

		//文本字符字体
		properties.put("kaptcha.textproducer.font.names","宋体,楷体,微软雅黑");

		Config config = new Config(properties);
		producer.setConfig(config);
		return producer;
	}


}
