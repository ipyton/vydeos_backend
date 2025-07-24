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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class AuthorizationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationService.class);

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
        logger.info("Reloading authorization paths");
        allowedPaths.clear();
        List<Path> allPaths = getAllPaths();
        logger.debug("Retrieved {} paths for reload", allPaths.size());

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
                    logger.info("Role {} has access to path: {}", entry.getKey(), path);
                }
            }

        logger.info("Authorization paths reload completed. Total roles: {}", allowedPaths.size());
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing AuthorizationService");

        try {
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
            logger.info("AuthorizationService initialization completed successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize AuthorizationService", e);
            throw new RuntimeException("AuthorizationService initialization failed", e);
        }
    }

    public List<Path> getAllPaths() {
        logger.debug("Retrieving all paths from database");
        try {
            ResultSet execute = session.execute(getAllPaths.bind());
            List<Path> parsedPaths = PathMapper.parsePaths(execute);
            List<PathDTO> pathDTOs = new ArrayList<>();

            parsedPaths.forEach(parsedPath -> {
                logger.trace("Processing path: {}", parsedPath.toString());
                pathDTOs.add(PathDTO.fromPath(parsedPath));
            });

            logger.debug("Retrieved {} paths successfully", parsedPaths.size());
            return parsedPaths;
        } catch (Exception e) {
            logger.error("Error retrieving all paths", e);
            throw new RuntimeException("Failed to retrieve paths", e);
        }
    }

    public Integer getRoleIdByEmail(String email) {
        logger.debug("Getting role ID for email: {}", email);
        try {
            ResultSet execute = session.execute(getRoleId.bind(email));
            List<Row> all = execute.all();

            if (all.isEmpty()) {
                logger.warn("No role found for email: {}", email);
                return null;
            }

            Row row = all.get(0);
            Integer roleId = row.getInt("roleid");
            logger.debug("Found role ID {} for email: {}", roleId, email);
            return roleId;
        } catch (Exception e) {
            logger.error("Error getting role ID for email: {}", email, e);
            throw new RuntimeException("Failed to get role ID", e);
        }
    }

    public boolean hasPermissionForUser(String userId, String permission) {
        logger.info("Checking permission '{}' for user: {}", permission, userId);
        try {
            Integer roleIdByEmail = getRoleIdByEmail(userId);
            if (roleIdByEmail == null) {
                logger.warn("No role found for user: {}", userId);
                return false;
            }

            boolean hasAccess = hasAccess(roleIdByEmail, permission);
            logger.info("User {} has access to '{}': {}", userId, permission, hasAccess);
            return hasAccess;
        } catch (Exception e) {
            logger.error("Error checking permission for user: {}", userId, e);
            return false;
        }
    }

    public List<PathDTO> getUIPathsByEmail(String email) {
        logger.debug("Getting UI paths for email: {}", email);
        try {
            Integer roleid = getRoleIdByEmail(email);
            if (roleid == null) {
                logger.warn("No role found for email: {}", email);
                return new ArrayList<>();
            }

            List<PathDTO> uiPathsByRoleId = getUIPathsByRoleId(roleid);
            logger.debug("Found {} UI paths for email: {}", uiPathsByRoleId.size(), email);
            return uiPathsByRoleId;
        } catch (Exception e) {
            logger.error("Error getting UI paths for email: {}", email, e);
            return new ArrayList<>();
        }
    }

    public List<PathDTO> getPathsByRoleId(Integer roleId) {
        logger.debug("Getting paths for role ID: {}", roleId);
        try {
            ResultSet execute = session.execute(getPathsByRoleId.bind(roleId));
            List<Path> parsedPaths = PathMapper.parsePaths(execute);
            List<PathDTO> pathDTOs = new ArrayList<>();

            parsedPaths.forEach(parsedPath -> {
                pathDTOs.add(PathDTO.fromPath(parsedPath));
            });

            logger.debug("Retrieved {} paths for role ID: {}", pathDTOs.size(), roleId);
            return pathDTOs;
        } catch (Exception e) {
            logger.error("Error getting paths for role ID: {}", roleId, e);
            throw new RuntimeException("Failed to get paths for role", e);
        }
    }

    public boolean hasAccess(int roleId, String path) {
        logger.debug("Checking access for role ID {} to path: {}", roleId, path);

        if (allowedPaths == null) {
            logger.warn("Allowed paths is null, denying access");
            return false;
        }

        List<String> patterns = allowedPaths.get(roleId);
        if (patterns == null) {
            logger.debug("No patterns found for role ID: {}", roleId);
            return false;
        }

        boolean match = PathMatcher.match(patterns, path);
        logger.debug("Access check result for role {} to path '{}': {}", roleId, path, match);
        return match;
    }

    public List<PathDTO> getUIPathsByRoleId(Integer roleId) {
        logger.debug("Getting UI paths for role ID: {}", roleId);
        try {
            ResultSet paths = session.execute(getPathsByRoleIdAndType.bind(roleId, "nav"));
            List<Path> parsedPaths = PathMapper.parsePaths(paths);
            ArrayList<PathDTO> result = new ArrayList<>();

            parsedPaths.forEach(parsedPath -> {
                result.add(PathDTO.fromPath(parsedPath));
            });

            logger.debug("Retrieved {} UI paths for role ID: {}", result.size(), roleId);
            return result;
        } catch (Exception e) {
            logger.error("Error getting UI paths for role ID: {}", roleId, e);
            throw new RuntimeException("Failed to get UI paths", e);
        }
    }

    public boolean upsertRole(Integer roleId, String roleName, List<PathDTO> paths) {
        logger.info("Upserting role: {} with name: {} and {} paths", roleId, roleName, paths.size());
        try {
            BatchStatementBuilder builder = BatchStatement.builder(DefaultBatchType.LOGGED);
            ResultSet execute = session.execute(getRoleById.bind(roleId));

            if (execute.all().isEmpty()) {
                logger.debug("Creating new role: {}", roleId);
                execute = session.execute(createRole.bind(roleId, roleName));
                if (!execute.getExecutionInfo().getErrors().isEmpty()) {
                    logger.error("Failed to create role: {}", roleId);
                    return false;
                }
            } else {
                logger.debug("Role {} already exists, updating paths", roleId);
            }

            for (PathDTO path : paths) {
                builder.addStatement(insertPath.bind(roleId, path.getType(), path.getRoute(), path.getName(), 0));
            }

            session.execute(builder.build());
            reload();
            logger.info("Successfully upserted role: {}", roleId);
            return true;
        } catch (Exception e) {
            logger.error("Error upserting role: {}", roleId, e);
            return false;
        }
    }

    public List<Role> getAllRoles() {
        logger.debug("Retrieving all roles");
        try {
            ResultSet resultSet = session.execute(getAllRoles.bind());
            List<Role> roles = new ArrayList<>();

            resultSet.forEach(row -> {
                int roleId = row.getInt("roleId");
                String roleName = row.getString("roleName");
                roles.add(new Role(roleId, roleName));
            });

            logger.debug("Retrieved {} roles", roles.size());
            return roles;
        } catch (Exception e) {
            logger.error("Error retrieving all roles", e);
            throw new RuntimeException("Failed to retrieve roles", e);
        }
    }

    public boolean updateUserRole(String userId, int newRoleId) {
        logger.info("Updating user role for user: {} to role: {}", userId, newRoleId);
        try {
            // Check if user exists
            ResultSet resultSet = session.execute("SELECT roleId FROM userinfo.user_auth WHERE userId = ?", userId);
            if (resultSet.all().isEmpty()) {
                logger.warn("User not found: {}", userId);
                return false;
            }

            // Check if role exists
            ResultSet resultSet1 = session.execute("SELECT roleId FROM userinfo.roles WHERE roleid = ?", newRoleId);
            if (resultSet1.all().isEmpty()) {
                logger.warn("Role not found: {}", newRoleId);
                return false;
            }

            PreparedStatement updateRoleStmt = session.prepare("UPDATE userinfo.user_auth SET roleId = ? WHERE userId = ?");
            BoundStatement boundStatement = updateRoleStmt.bind(newRoleId, userId);
            session.execute(boundStatement);

            // Verify update
            resultSet = session.execute("SELECT roleId FROM userinfo.user_auth WHERE userId = ?", userId);
            Row row = resultSet.one();
            if (row != null && row.getInt("roleId") == newRoleId) {
                logger.info("Successfully updated user role for user: {} to role: {}", userId, newRoleId);
                return true;
            }

            logger.error("Failed to verify role update for user: {}", userId);
            return false;
        } catch (Exception e) {
            logger.error("Error updating user role for user: {}", userId, e);
            return false;
        }
    }

    public boolean DeleteUserRole(int roleId) {
        logger.info("Deleting user role: {}", roleId);
        String deleteQuery = "DELETE FROM userInfo.roles WHERE roleid = ?";
        try {
            session.execute(deleteQuery, roleId);
            logger.info("Successfully deleted user role: {}", roleId);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting user role: {}", roleId, e);
            return false;
        }
    }

    public boolean deletePath(Integer roleId, String path, String type) {
        logger.info("Deleting path: {} of type: {} for role: {}", path, type, roleId);
        try {
            ResultSet execute = session.execute(deletePath.bind(roleId, path, type));
            if (execute.getExecutionInfo().getErrors().isEmpty()) {
                logger.info("Successfully deleted path: {} for role: {}", path, roleId);
                return true;
            } else {
                logger.error("Failed to delete path: {} for role: {}", path, roleId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error deleting path: {} for role: {}", path, roleId, e);
            return false;
        }
    }
}