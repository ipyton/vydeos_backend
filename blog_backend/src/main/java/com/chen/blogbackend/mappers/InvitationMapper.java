package com.chen.blogbackend.mappers;


import com.chen.blogbackend.entities.Invitation;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;


import java.util.LinkedList;
import java.util.List;


public class InvitationMapper {
    public static List<Invitation> parseInvitation(ResultSet resultSet) {
        List<Invitation> invitations = new LinkedList<>();

        for (Row row : resultSet) {
            Invitation invitation = new Invitation();

            invitation.setType(row.getString("type"));
            invitation.setGroupId(row.isNull("group_id") ? null : row.getLong("group_id"));
            invitation.setUserId(row.getString("user_id"));
            invitation.setExpireTime(row.getInstant("expire_time"));
            invitation.setCode(row.getString("code"));
            invitation.setCreateTime(row.getInstant("create_time"));

            invitations.add(invitation);
        }

        return invitations;
    }

}
