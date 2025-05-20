package com.chen.blogbackend.entities;

public class Country {
    private String country;
    private String language;
    private String country_en;

    public Country(String country, String language, String country_en) {
        this.country = country;
        this.language = language;
        this.country_en = country_en;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCountry_en() {
        return country_en;
    }

    public void setCountry_en(String country_en) {
        this.country_en = country_en;
    }
}
