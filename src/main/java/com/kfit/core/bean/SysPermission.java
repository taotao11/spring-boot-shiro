package com.kfit.core.bean;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

/**
 * 权限
 * @author Angel -- 守护天使
 * @version v.0.1
 * @date 2017年10月13日
 */
@Entity
public class SysPermission implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Id @GeneratedValue
	private long id;//主键.
	private String name;//权限名称.
	@Column(columnDefinition="enum('menu','button')")
	private String resourceType;//资源类型,[menu|button]
	private String url;//资源路径.
	private String permission;////权限字符串,menu例子：role:*，button例子：role:create,role:update,role:delete,role:view
	private Long parentId;//父编号.
	private String parentIds;//父编号列表.
	private Boolean available = Boolean.FALSE;
	
	//权限 - 角色 : 多对多
	@ManyToMany
	@JoinTable(name="SysRolePermission",joinColumns={@JoinColumn(name="permissionId")},inverseJoinColumns={@JoinColumn(name="roleId")})
	private List<SysRole> roles;//角色.

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getParentIds() {
		return parentIds;
	}

	public void setParentIds(String parentIds) {
		this.parentIds = parentIds;
	}

	public Boolean getAvailable() {
		return available;
	}

	public void setAvailable(Boolean available) {
		this.available = available;
	}

	public List<SysRole> getRoles() {
		return roles;
	}

	public void setRoles(List<SysRole> roles) {
		this.roles = roles;
	}
}	
