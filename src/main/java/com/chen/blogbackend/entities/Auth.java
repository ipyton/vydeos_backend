package com.chen.blogbackend.entities;

public class Auth {
    private String userId;
    private String password;
    private String email;
    private String telephone;

    public Auth(String userId, String password, String email, String telephone) {
        this.userId = userId;
        this.password = password;
        this.email = email;
        this.telephone = telephone;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
