package com.chen.blogbackend.entities;

import java.util.List;
import java.util.UUID;

public class Role {
    private int roleId;
    private String roleName;
    private List<String> allowedPaths;

    // Constructors
    public Role(int roleId, String roleName, List<String> allowedPaths) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.allowedPaths = allowedPaths;
    }

    // Getters and Setters
    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<String> getAllowedPaths() {
        return allowedPaths;
    }

    public void setAllowedPaths(List<String> allowedPaths) {
        this.allowedPaths = allowedPaths;
    }
}
