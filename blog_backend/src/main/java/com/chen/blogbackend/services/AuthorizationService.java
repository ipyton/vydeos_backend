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
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
    PreparedStatement getRoleById;
    PreparedStatement deletePath;

    public static HashMap<Integer, List<String>> allowedPaths;



    public void reload() {
        allowedPaths.clear();
        List<Path> allPaths = getAllPaths();
        for (Path item : allPaths) {
            if (item.getType().equals("api")) {
                if (allowedPaths.containsKey(item.getRoleId())) {
                    allowedPaths.get(item.getRoleId()).add(item.getPath());
                }
                else {
                    allowedPaths.put(item.getRoleId(), new ArrayList<>());
                    allowedPaths.get(item.getRoleId()).add(item.getPath());
                }
            }
        }
        for (Map.Entry<Integer, List<String>> entry : allowedPaths.entrySet()) {
            for (String path : entry.getValue()) {
                System.out.println(entry.getKey() + ":" + path);
            }
        }
    }

    @PostConstruct
    public void init() {
        getRoleId = session.prepare("select roleid from userinfo.user_auth where userid = ?;");

        getAllRoles = session.prepare("select roleId, roleName from userinfo.roles allow filtering;");
        createRole = session.prepare("insert into userinfo.roles (roleid, roleName) values (?, ?);");
        deleteRole = session.prepare("delete from userinfo.roles where roleid = ?;");
        getRoleById = session.prepare("select * from userinfo.roles where roleid = ?");

        getPathsByRoleId = session.prepare("select * from userinfo.role_auths where roleid = ?;");
        getPathsByRoleIdAndType = session.prepare("select * from userinfo.role_auths where roleid = ? and type = ?");
        insertPath = session.prepare("insert into userinfo.role_auths ( roleid ,type, path, name, version) values (?,?, ?, ?, ?);");
        getAllPaths = session.prepare("select * from userinfo.role_auths allow filtering;");
        deletePath = session.prepare("delete from userinfo.role_auths where roleid=? and path = ? and type = ?;");
        allowedPaths = new HashMap<>();
        reload();
    }


    public List<Path> getAllPaths() {
        ResultSet execute = session.execute(getAllPaths.bind());
        List<Path> parsedPaths = PathMapper.parsePaths(execute);
        List<PathDTO> pathDTOs = new ArrayList<>();
        parsedPaths.forEach(parsedPath -> {
            System.out.println(parsedPath.toString());
            pathDTOs.add(PathDTO.fromPath(parsedPath));
        });
        return parsedPaths;
    }

    public Integer getRoleIdByEmail(String email) {
        ResultSet execute = session.execute(getRoleId.bind(email));
        List<Row> all = execute.all();
        Row row = all.get(0);
        return row.getInt("roleid");
    }


    public boolean hasPermissionForUser(String userId, String permission) {
        Integer roleIdByEmail = getRoleIdByEmail(userId);
        return hasAccess(roleIdByEmail, permission);
    }


    public List<PathDTO> getUIPathsByEmail(String Email) {
        Integer roleid = getRoleIdByEmail(Email);
        List<PathDTO> uiPathsByRoleId = getUIPathsByRoleId(roleid);
        return uiPathsByRoleId;
    }

    public List<PathDTO> getPathsByRoleId(Integer roleId) {
        ResultSet execute = session.execute(getPathsByRoleId.bind(roleId));
        List<Path> parsedPaths = PathMapper.parsePaths(execute);
        List<PathDTO> pathDTOs = new ArrayList<>();
        parsedPaths.forEach(parsedPath -> {
            pathDTOs.add(PathDTO.fromPath(parsedPath));
        });
        return pathDTOs;
    }

    public boolean hasAccess(int roleId, String path) {
        // 1. 查询角色权限
        System.out.println(allowedPaths.toString());
        if (allowedPaths == null) {
            return false;
        }
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




    public boolean upsertRole(Integer roleId, String roleName, List<PathDTO> paths) {
        BatchStatementBuilder builder = BatchStatement.builder(DefaultBatchType.LOGGED);
        ResultSet execute = session.execute(getRoleById.bind(roleId));
        if (execute.all().isEmpty()) {
            execute = session.execute(createRole.bind(roleId, roleName));
            if (!execute.getExecutionInfo().getErrors().isEmpty()) {
                return false;
            }
        }
        for (PathDTO path : paths) {
            builder.addStatement(insertPath.bind(roleId, path.getType(), path.getRoute() , path.getName(),0));
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


    public boolean deletePath(Integer roleId, String path, String type) {
        try {
            ResultSet execute = session.execute(deletePath.bind(roleId, path, type));
            if (execute.getExecutionInfo().getErrors().isEmpty()) {
                return true;
            }
            else {
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error deleting path: " + e.getMessage());
            return false;
        }

    }
}
