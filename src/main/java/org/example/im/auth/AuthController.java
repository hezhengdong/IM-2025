package org.example.im.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.example.im.common.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired private AuthService authService;

    @PostMapping("/register")
    public Result register(@RequestBody AuthRequest request) {
        return authService.register(request.getUsername(), request.getPassword()) ?
            Result.success() :
            Result.error("注册失败");
    }

    @PostMapping("/login")
    public Result login(@RequestBody AuthRequest request, HttpServletResponse response) {
        Integer userId = authService.login(request.getUsername(), request.getPassword());
        if (userId == null) {
            return Result.error("登录失败");
        }

        // 设置响应头
        response.setHeader("userId", userId.toString());

        return Result.success();
    }
}
