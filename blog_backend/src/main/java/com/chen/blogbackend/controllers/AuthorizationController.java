package com.chen.blogbackend.controllers;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.entities.PathDTO;
import com.chen.blogbackend.entities.Role;
import com.chen.blogbackend.entities.RoleDTO;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.Message;
import com.chen.blogbackend.services.AuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Controller()
@RequestMapping("auth")
@ResponseBody
public class AuthorizationController {

    @Autowired
    AuthorizationService service;


    @RequestMapping("getNavPaths")
    public Message getUIPaths(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userEmail");
            List<PathDTO> uiPathsByEmail = service.getUIPathsByEmail(userId);
            return new Message(0, JSON.toJSONString(uiPathsByEmail));
        }
        catch (Exception e) {
            e.printStackTrace();
            return new Message(-1, e.getMessage());
        }
    }

    @RequestMapping("getPathsByRoleId")
    public Message getUIPathsByRoleId(Integer roleId) {
        try {
            List<PathDTO> uiPathsByRoleId = service.getPathsByRoleId(roleId);
            return new Message(0, JSON.toJSONString(uiPathsByRoleId));
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(-1, e.getMessage());
        }
    }

    @PostMapping("/upsertRole")
    public LoginMessage upsertRole(@RequestBody RoleDTO roleDTO) {
        Integer roleId = roleDTO.getRoleId();
        String roleName = roleDTO.getRoleName();
        List<PathDTO> allowedPaths = roleDTO.getAllowedPaths();
        System.out.println(roleId + ":" + roleName + ":" + allowedPaths);
        if ( allowedPaths == null || roleName == null || roleId == null ) return new LoginMessage(-1, "parameters are missing");
        boolean result = service.upsertRole(roleId, roleName, allowedPaths);
        if (result) {
            return new LoginMessage(0, "Success");
        }
        else {
            return new LoginMessage(-1, " error");
        }
    }

    @PostMapping("deletePath")
    public Message deletePath(@RequestBody RoleDTO roleDTO) {

        if ( roleDTO == null || roleDTO.getAllowedPaths().size() == 0 || roleDTO.getRoleId() == null ) return new Message(-1, "parameters are missing");

        try {
            PathDTO pathDTO = roleDTO.getAllowedPaths().get(0);

            boolean result = service.deletePath(roleDTO.getRoleId(), pathDTO.getRoute(), pathDTO.getType());
            if (result) {
                return new Message(0, "Success");
            }
            else {
                return new Message(-1, "error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(-1, e.getMessage());
        }


    }

    @GetMapping("/getRole")
    public List<Role> getRole() {
        return service.getAllRoles();
    }



    @PostMapping("/deleteRole")
    public LoginMessage deleteRole(int roleId) {
        boolean b = service.DeleteUserRole(roleId);
        if (b) {
            return new LoginMessage(0, "Success");
        }
        return new LoginMessage(-1, "delete error");
    }

    @PostMapping("/changeRole")
    public LoginMessage changeRole(HttpServletRequest request,String userId, int roleId) {

        boolean b = service.updateUserRole(userId, roleId);
        if (b) {
            return new LoginMessage(0, "Success");
        } else {
            return new LoginMessage(-1, "change role error");
        }
    }

    @PostMapping("reload")
    public LoginMessage reload() {
        service.reload();
        return new LoginMessage(0, "Success");
    }

    @PostMapping("hasPermission")
    public Message hasPermission(HttpServletRequest servletRequest, @RequestBody Map<String, Object> payload) {
        try {
            String path = (String) payload.get("path");
            if (null == path || path.length() == 0) return new Message(-1, "path is null");
            String userEmail = (String) servletRequest.getAttribute("userEmail");
            boolean b = service.hasPermissionForUser(userEmail, path);
            if (b) {
                return new Message(0, "Success");
            } else {
                return new Message(-1, "does not have permission");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(-1, e.getMessage());
        }

    }



}
