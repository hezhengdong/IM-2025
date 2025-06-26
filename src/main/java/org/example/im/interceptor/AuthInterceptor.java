package org.example.im.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.im.auth.UserRepository;
import org.example.im.common.dto.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);
    
    @Autowired
    private UserRepository userRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        logger.info("认证拦截器拦截请求: {}", requestURI);

        // 获取userId请求头
        String userIdHeader = request.getHeader("userId");
        
        // 检查userId是否存在
        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            logger.warn("请求缺少userId请求头: {}", requestURI);
            returnUnauthorized(response, "请先登录");
            return false;
        }

        try {
            // 验证userId格式
            Integer userId = Integer.valueOf(userIdHeader.trim());
            
            // 检查用户是否存在于数据库中
            if (!userRepository.existsById(userId)) {
                logger.warn("用户不存在: userId={}, URI={}", userId, requestURI);
                returnUnauthorized(response, "用户不存在，请重新登录");
                return false;
            }
            
            logger.info("用户认证成功: userId={}", userId);
            return true;
            
        } catch (NumberFormatException e) {
            logger.warn("userId格式错误: {}, URI={}", userIdHeader, requestURI);
            returnUnauthorized(response, "用户身份验证失败");
            return false;
        }
    }

    private void returnUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        Result result = Result.error(message);
        String jsonResponse = objectMapper.writeValueAsString(result);
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
