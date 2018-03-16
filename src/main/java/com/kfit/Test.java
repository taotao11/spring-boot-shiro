package com.kfit;

import org.apache.shiro.crypto.hash.Md5Hash;

public class Test {

	public static void main(String[] args) {
		
		/**
		 * 密码加密算法.
		 * 
		 */
		//原始密码
		String source = "123456";
		//账号.
		String username = "test";
		//盐:混淆密码.
		String salt = username + "237755f200a3bdfe716da0eae0b4d909";//可以固定的，或者随机的.
		//期望加密之后的值是：6a62cd4f0b80232f6a86070197841d00
		int hashIterations = 2;//散列的次数，也就是散列两次的话，相当于md5(md5("123456"))
		Md5Hash md5Hash = new Md5Hash(source, salt, hashIterations);
		String passwrod_md5 = md5Hash.toString();
		System.out.println(passwrod_md5);
	}

}
