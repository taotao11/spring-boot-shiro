package com.kfit.core.repository;

import org.springframework.data.repository.CrudRepository;

import com.kfit.core.bean.UserInfo;

public interface UserInfoRepository extends CrudRepository<UserInfo, Long> {
	/**通过username查找用户信息;*/
	public UserInfo findByUsername(String username);
}
