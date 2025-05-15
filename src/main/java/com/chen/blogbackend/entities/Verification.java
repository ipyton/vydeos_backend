package com.chen.blogbackend.entities;

import java.time.Instant;

public class Verification {
    private String code;
    private String email;
    private Instant expiration;

    public Verification(String code, String email, Instant expiration) {
        this.code = code;
        this.email = email;
        this.expiration = expiration;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getExpiration() {
        return expiration;
    }

    public void setExpiration(Instant expiration) {
        this.expiration = expiration;
    }
}
