package com.chen.blogbackend.controllers;

import com.chen.blogbackend.entities.Path;
import com.chen.blogbackend.entities.PathDTO;
import com.chen.blogbackend.entities.Role;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.services.AuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller()
@RequestMapping("auth")
@ResponseBody
public class AuthorizationController {

    @Autowired
    AuthorizationService service;

    @RequestMapping("getPaths")
    public List<PathDTO> getPaths(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userEmail");
        return service.getPaths(userId);
    }

    @PostMapping("/upsertRole")
    public LoginMessage upsertRole(List<Path> role) {
        if (role == null) return new LoginMessage(-1, "role is null");
        boolean result = service.upsertRole(role);
        if (result) {
            return new LoginMessage(1, "Success");
        }
        else {
            return new LoginMessage(-1, "reset password error");
        }
    }

    @GetMapping("/getRole")
    public List<Role> getRole() {
        return service.getRoles();
    }

    @PostMapping("/deleteRole")
    public LoginMessage deleteRole(int roleId) {
        boolean b = service.DeleteUserRole(roleId);
        if (b) {
            return new LoginMessage(1, "Success");
        }
        return new LoginMessage(-1, "delete error");
    }

    @PostMapping("/changeRole")
    public LoginMessage changeRole(HttpServletRequest request,String userId, int roleId) {

        boolean b = service.updateUserRole(userId, roleId);
        if (b) {
            return new LoginMessage(1, "Success");
        } else {
            return new LoginMessage(-1, "change role error");
        }
    }

}
