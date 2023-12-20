package com.chen.blogbackend.DAO;

import com.chen.blogbackend.entities.ChatGroup;
import com.chen.blogbackend.entities.ChatGroupMember;
import com.chen.blogbackend.mappers.ChatGroupMapper;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.mapper.annotations.GetEntity;

public interface ChatGroupMemberDao {
    @GetEntity
    PagingIterable<ChatGroupMember> convert(ResultSet set);
}
