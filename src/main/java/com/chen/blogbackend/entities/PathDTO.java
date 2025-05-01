package com.chen.blogbackend.entities;

/**
 * Data Transfer Object for Role entity
 * Contains only the necessary fields for client-side representation
 */
public class PathDTO {
    private String name;
    private String route;
    private String iconName;

    // Default constructor
    public PathDTO() {
    }

    // Constructor with fields
    public PathDTO(String name, String route, String iconName) {
        this.name = name;
        this.route = route;
        this.iconName = iconName;
    }

    // Factory method to convert Role entity to RoleDTO
    public static PathDTO fromPath(Path role) {
        return new PathDTO(
                role.getName(),
                role.getPath(),  // 'path' in entity maps to 'route' in DTO
                role.getIconName()
        );
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    @Override
    public String toString() {
        return "RoleDTO{" +
                "name='" + name + '\'' +
                ", route='" + route + '\'' +
                ", iconName='" + iconName + '\'' +
                '}';
    }
}