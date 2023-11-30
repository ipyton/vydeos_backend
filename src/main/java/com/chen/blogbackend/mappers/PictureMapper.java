package com.chen.blogbackend.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;

@Mapper
public interface PictureMapper {

    @Select("select picture_amount from article where article_id=#{articleID}")
    public Integer getPictureAmountByArticleID(String articleID);

}
