package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Path;
import com.chen.blogbackend.entities.PathDTO;
import com.chen.blogbackend.entities.Role;
import com.chen.blogbackend.mappers.PathMapper;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthorizationService {

    @Autowired
    CqlSession session;

    PreparedStatement getRoleId;
    PreparedStatement getPaths;
    PreparedStatement getAllRoles;
    PreparedStatement insertPath;

    @PostConstruct
    public void init() {
        getRoleId = session.prepare("select roleid from userinfo.user_auth where userid = ?;");
        getPaths = session.prepare("select * from userinfo.roles where roleid = ?;");
        getAllRoles = session.prepare("select roleId, role_name from userinfo.roles allow filtering;");
        insertPath = session.prepare("insert into userinfo.roles ( roleid, role_name, path, name, version) values (?, ?, ?, ?, ?);");

    }

    public List<PathDTO> getPaths(String userId) {
        try {
            ResultSet execute = session.execute(getRoleId.bind(userId));
            Integer roleId = execute.all().get(0).getInt("roleid");
            ResultSet paths = session.execute(getPaths.bind(roleId));

            List<Path> parsedPaths = PathMapper.parsePaths(paths);
            ArrayList<PathDTO> result = new ArrayList<>();
            parsedPaths.forEach(parsedPath -> {
                result.add(PathDTO.fromPath(parsedPath));
            });
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public boolean upsertRole(List<Path> paths) {
        BatchStatementBuilder builder = BatchStatement.builder(DefaultBatchType.LOGGED);

        for (Path path : paths) {
            builder.addStatement(insertPath.bind(path.getRoleId(), path.getName(), path.getPath(),0));
        }
        session.execute(builder.build());
        return true;
    }



    public List<Role> getRoles() {

        ResultSet resultSet = session.execute(getAllRoles.bind());

        List<Role> roles = new ArrayList<>();

        // Iterate over the result set and convert rows to Role objects
        resultSet.forEach(row -> {
            int roleId = row.getInt("roleId");
            String roleName = row.getString("roleName");
            roles.add(new Role(roleId, roleName));
        });

        return roles;
    }

    public boolean updateUserRole( String userId, int newRoleId) {
        // 1. 检查用户是否存在
        ResultSet resultSet = session.execute("SELECT roleId FROM userinfo.user_auth WHERE userId = ?", userId);
        if (resultSet.all().isEmpty()) {
            // 如果没有找到该用户，返回 false
            return false;
        }
        ResultSet resultSet1 = session.execute("SELECT roleId FROM userinfo.roles WHERE roleid = ?", newRoleId);
        if (resultSet1.all().isEmpty()) {
            return false;
        }
        // 2. 如果用户存在，执行更新操作
        // 更新用户的 roleId
        PreparedStatement updateRoleStmt = session.prepare("UPDATE userinfo.user_auth SET roleId = ? WHERE userId = ?");
        BoundStatement boundStatement = updateRoleStmt.bind(newRoleId, userId);
        session.execute(boundStatement);

        // 3. 确认是否更新成功
        resultSet = session.execute("SELECT roleId FROM userinfo.user_auth WHERE userId = ?", userId);
        Row row = resultSet.one();
        if (row != null && row.getInt("roleId") == newRoleId) {
            return true; // 更新成功
        }

        return false; // 更新失败
    }

    public boolean DeleteUserRole(int roleId) {
        String deleteQuery = "DELETE FROM userInfo.roles WHERE roleid = ?";
        try {
            session.execute(deleteQuery, roleId);
            return true; // 删除成功
        } catch (Exception e) {
            System.err.println("Error deleting user role: " + e.getMessage());
            return false; // 删除失败
        }
    }


}
