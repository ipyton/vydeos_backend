package com.chen.blogbackend.entities;

/**
 * Data Transfer Object for Role entity
 * Contains only the necessary fields for client-side representation
 */
public class PathDTO {
    private String name;
    private String route;
    private String type;


    // Default constructor
    public PathDTO() {
    }

    // Constructor with fields
    public PathDTO(String name, String route, String type) {
        this.name = name;
        this.route = route;
        this.type = type;
    }

    // Factory method to convert Role entity to RoleDTO
    public static PathDTO fromPath(Path role) {
        return new PathDTO(
                role.getName(),
                role.getPath(),  // 'path' in entity maps to 'route' in DTO
                role.getType()
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "RoleDTO{" +
                "name='" + name + '\'' +
                ", route='" + route + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}