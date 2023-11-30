package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Video;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VideoMapper {

    @Insert("insert into videos() values()")
    int insertVideo(Video video);

    @Delete("delete ")
    int deleteVideo(Video video);

}
