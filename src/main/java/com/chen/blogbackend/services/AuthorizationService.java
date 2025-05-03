package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Path;
import com.chen.blogbackend.entities.PathDTO;
import com.chen.blogbackend.entities.Role;
import com.chen.blogbackend.filters.LoginTokenFilter;
import com.chen.blogbackend.mappers.PathMapper;
import com.chen.blogbackend.util.PathMatcher;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class AuthorizationService {

    @Autowired
    CqlSession session;

    PreparedStatement getRoleId;
    PreparedStatement getAllPaths;
    PreparedStatement getAllRoles;
    PreparedStatement insertPath;
    PreparedStatement createRole;
    PreparedStatement deleteRole;
    PreparedStatement getPathsByRoleIdAndType;
    PreparedStatement getPathsByRoleId;


    public static HashMap<Integer, List<String>> allowedPaths;



    private void reload() {
        allowedPaths.clear();
        List<Path> allPaths = getAllPaths();
        for (Path item : allPaths) {
            if (item.getType().equals("api")) {
                if (allowedPaths.containsKey(item.getRoleId())) {
                    allowedPaths.get(item.getRoleId()).add(item.getPath());
                }
            }
        }
    }

    @PostConstruct
    public void init() {
        getRoleId = session.prepare("select roleid from userinfo.user_auth where userid = ?;");

        getAllRoles = session.prepare("select roleId, roleName from userinfo.roles allow filtering;");
        createRole = session.prepare("insert into userinfo.roles (roleid, roleName) values (?, ?);");
        deleteRole = session.prepare("delete from userinfo.roles where roleid = ?;");


        getPathsByRoleId = session.prepare("select * from userinfo.role_auths where roleid = ?;");
        getPathsByRoleIdAndType = session.prepare("select * from userinfo.role_auths where roleid = ? and type = ?");
        insertPath = session.prepare("insert into userinfo.role_auths ( roleid, path, name, version) values (?, ?, ?, ?, ?);");
        getAllPaths = session.prepare("select * from userinfo.role_auths allow filtering;");
        allowedPaths = new HashMap<>();
        reload();
    }


    public List<Path> getAllPaths() {
        ResultSet execute = session.execute(getAllPaths.bind());
        List<Path> parsedPaths = PathMapper.parsePaths(execute);
        List<PathDTO> pathDTOs = new ArrayList<>();
        parsedPaths.forEach(parsedPath -> {
            pathDTOs.add(PathDTO.fromPath(parsedPath));
        });
        return parsedPaths;
    }

    public List<PathDTO> getUIPathsByEmail(String Email) {
        ResultSet execute = session.execute(getRoleId.bind(Email));
        List<Row> all = execute.all();
        Row row = all.get(0);
        Integer roleid = row.getInt("roleid");
        List<PathDTO> uiPathsByRoleId = getUIPathsByRoleId(roleid);
        return uiPathsByRoleId;
    }

    public boolean hasAccess(int roleId, String path) {
        // 1. 查询角色权限

        List<String> patterns = allowedPaths.get(roleId);
        boolean match = PathMatcher.match(patterns, path);
        return match;
    }

    public List<PathDTO> getUIPathsByRoleId(Integer roleId) {
        try {
            ResultSet paths = session.execute(getPathsByRoleIdAndType.bind(roleId, "nav"));
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
        reload();
        return true;
    }



    public List<Role> getAllRoles() {

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
        PreparedStatement updateRoleStmt = session.prepare("UPDATE userinfo.user_auth SET roleId = ? WHERE userId = ?");
        BoundStatement boundStatement = updateRoleStmt.bind(newRoleId, userId);
        session.execute(boundStatement);

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
