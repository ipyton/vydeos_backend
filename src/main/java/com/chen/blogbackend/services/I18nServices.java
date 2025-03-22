package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Country;
import com.chen.blogbackend.mappers.CountryMapper;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.session.Session;
import jakarta.annotation.PostConstruct;
import org.checkerframework.checker.units.qual.A;
import org.graalvm.polyglot.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;


@Service
public class I18nServices {

    @Autowired
    private  CqlSession session;

    PreparedStatement getLanguages = null;

    @PostConstruct
    public void init() {
        getLanguages = session.prepare("select * from i18n.languages;");
    }
    public List<Country> getLanguages() {
        ResultSet execute = session.execute(getLanguages.bind());
        return CountryMapper.countriesMapper(execute);
    }

}
