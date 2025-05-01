package com.chen.blogbackend.entities;

public class Path {
    private int roleId;
    private String roleName;
    private String path;
    private String name;
    private String version;
    private String iconName;



    // Constructors
    public Path(int roleId, String roleName, String path, String name, String version, String iconName) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.path = path;
        this.name = name;
        this.version = version;
        this.iconName = iconName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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


}
