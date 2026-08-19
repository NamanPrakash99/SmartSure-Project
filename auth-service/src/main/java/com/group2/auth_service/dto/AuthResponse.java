package com.group2.auth_service.dto;

public class AuthResponse {

	private String token;
	private String role;
	private Long id;
	private String name;

	public AuthResponse(String token, String role, Long id, String name) {
		this.token = token;
		this.role = role;
		this.id = id;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	

	public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
	}



	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

}
