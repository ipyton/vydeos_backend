package com.chen.blogbackend.mappers;

import com.chen.blogbackend.DAO.ArticleDao;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;

public interface ChatGroupMapper {
    @DaoFactory
    ArticleDao getGroupDao();
}
