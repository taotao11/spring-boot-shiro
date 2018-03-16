package com.kfit.config.shiro;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 凭证匹配器
 *
 * 用于密码次数
 * 1 用缓存记录 记录密码输入次数
 *  1. 注入缓存对象
 *  2.获取当前登录的用户账号
 *  3.获取当前账号的登陆次数 Cache<String, AtomicInteger></>  AtomicInteger 是线程安全的 ,integer非线程安全
 *  4 大于3次 抛出异常
 *
 */
public class MyHashedCredentialsMatcher extends HashedCredentialsMatcher {

    private Cache<String, AtomicInteger> cache;

    public MyHashedCredentialsMatcher(CacheManager cacheManager) {
        this.cache = cacheManager.getCache("passwordRetryCache");
    }

    /**
     * 判断是否超过三次
     * @param token
     * @param info
     * @return
     */
    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        //获取用户信息
        String name = (String) token.getPrincipal();

        //获取到输入次数
        AtomicInteger retryCount = cache.get(name);

        if (retryCount == null){
            retryCount = new AtomicInteger(0);
            cache.put(name,retryCount);
        }
        //判断是否超过3次
        if(retryCount.incrementAndGet() > 3){
            //抛出异常
            throw new ExcessiveAttemptsException();
        }
        //验证
        boolean matches = super.doCredentialsMatch(token,info);
        //验证通过 清空缓存
        if (matches){
            cache.remove(name);
        }
        return super.doCredentialsMatch(token, info);
    }
}
