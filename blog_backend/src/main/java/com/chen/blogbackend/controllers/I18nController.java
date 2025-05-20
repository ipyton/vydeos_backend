package com.chen.blogbackend.controllers;

import com.chen.blogbackend.entities.Country;
import com.chen.blogbackend.services.I18nServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@ResponseBody
@RequestMapping("i18n")
public class I18nController {

    @Autowired
    I18nServices i18nServices;

    @RequestMapping("getLanguages")
    public List<Country> getLanguages() {
        return i18nServices.getLanguages();
    }

}
