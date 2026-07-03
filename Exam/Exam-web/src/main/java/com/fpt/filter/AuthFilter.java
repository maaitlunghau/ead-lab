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

@WebFilter(urlPatterns = { "*.xhtml" })
public class AuthFilter implements Filter {

    private static final String[] PUBLIC_PAGES = {
            "search.xhtml",
            "javax.faces.resource"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String requestURI = httpReq.getRequestURI();

        for (String page : PUBLIC_PAGES) {
            if (requestURI.contains(page)) {
                chain.doFilter(request, response);
                return;
            }
        }

        HttpSession session = httpReq.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("customer") != null;

        if (loggedIn) {
            chain.doFilter(request, response);
        } else {
            httpResp.sendRedirect(httpReq.getContextPath() + "/search.xhtml");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
