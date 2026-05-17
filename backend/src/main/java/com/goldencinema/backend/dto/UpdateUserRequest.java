package com.goldencinema.backend.dto;

public class UpdateUserRequest {

    private Boolean isActive;
    private String role;

    public UpdateUserRequest() {}

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
