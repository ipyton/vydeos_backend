package com.chen.blogbackend.entities;

import java.util.List;

public class RoleDTO {
    private Integer roleId;
    private String roleName;
    private List<PathDTO> allowedPaths;

    // Getters and setters
    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<PathDTO> getAllowedPaths() {
        return allowedPaths;
    }

    public void setAllowedPaths(List<PathDTO> allowedPaths) {
        this.allowedPaths = allowedPaths;
    }
}
