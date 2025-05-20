package com.chen.blogbackend.entities;

public class Path {
    private Integer roleId;
    private String roleName;
    private String path;
    private String name;
    private Integer version;
    private String iconName;
    private String type;


    // Constructors
    public Path(int roleId, String roleName, String path, String name, Integer version, String iconName, String type) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.path = path;
        this.name = name;
        this.version = version;
        this.iconName = iconName;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Path{" +
                "roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                ", path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", version=" + version +
                ", iconName='" + iconName + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
