package com.chen.blogbackend.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;

@Mapper
public interface PictureMapper {

    @Select("select pic_id from article where p")
    public ArrayList<String> getPictureAddressByArticleID(String articleID);

    @Select("select count(*) from article where article_id=#{articleID}")
    public Integer getPictureAmountByArticleID(String articleID);

}
