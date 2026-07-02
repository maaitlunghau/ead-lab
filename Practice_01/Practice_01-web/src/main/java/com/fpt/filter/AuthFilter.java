package com.fpt.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = {"*.xhtml"})
public class AuthFilter implements Filter {

    // Các trang không cần đăng nhập
    private static final String[] PUBLIC_PAGES = {
        "login.xhtml",
        "javax.faces.resource"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpReq  = (HttpServletRequest)  request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String requestURI = httpReq.getRequestURI();

        // Cho qua nếu là trang public
        for (String page : PUBLIC_PAGES) {
            if (requestURI.contains(page)) {
                chain.doFilter(request, response);
                return;
            }
        }

        // Kiểm tra session có loggedInUser không
        HttpSession session = httpReq.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("loggedInUser") != null;

        if (loggedIn) {
            chain.doFilter(request, response);
        } else {
            // Chưa đăng nhập → redirect về login
            String loginPage = httpReq.getContextPath() + "/login.xhtml";
            httpResp.sendRedirect(loginPage);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
