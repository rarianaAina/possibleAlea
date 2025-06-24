package com.example.erpnextintegration.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Intercepteur pour vérifier l'authentification des utilisateurs.
 */
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        
        // Vérifier si l'utilisateur est authentifié
        if (session.getAttribute("apiKey") == null || session.getAttribute("apiSecret") == null) {
            response.sendRedirect("/login");
            return false;
        }
        
        return true;
    }
}