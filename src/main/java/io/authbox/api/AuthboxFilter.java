package io.authbox.api;

import io.authbox.api.AuthboxConfig;
import io.authbox.api.AuthboxHttpServletRequestWrapper;
import io.authbox.api.AuthboxHttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthboxFilter implements Filter {
    private final double RELINK_PROBABILITY = 0.01;
    private FilterConfig filterConfig = null;
    private Random random;

    public void init(FilterConfig filterConfig)
        throws ServletException {
        this.filterConfig = filterConfig;
        random = new Random();
    }

    public void destroy() {
        this.filterConfig = null;
    }

    private boolean didGetPixel(HttpServletRequest request, String didGetPixelCookieName) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return false;
        }

        for (Cookie cookie: cookies) {
            if (cookie.getName().equals(didGetPixelCookieName)) {
                return true;
            }
        }
        return false;
    }

    public void doFilter(ServletRequest request,
                         ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        if (filterConfig == null) {
            return;
        }

        PrintWriter out = response.getWriter();
        AuthboxHttpServletRequestWrapper requestWrapper = new AuthboxHttpServletRequestWrapper((HttpServletRequest)request);

        String localMachineID = requestWrapper.getLocalMachineID();
        Cookie localMachineIDCookie = new Cookie(requestWrapper.getLocalMachineIDCookieName(), localMachineID);
        localMachineIDCookie.setPath("/");
        localMachineIDCookie.setHttpOnly(true);
        localMachineIDCookie.setMaxAge(2 * 365 * 24 * 60 * 60);
        ((HttpServletResponse)response).addCookie(localMachineIDCookie);

        String didGetPixelCookieName = requestWrapper.getDidGetPixelCookieName();
        boolean renderPixel = (!didGetPixel((HttpServletRequest)request, didGetPixelCookieName)) || random.nextDouble() < RELINK_PROBABILITY;

        AuthboxHttpServletResponseWrapper responseWrapper = new AuthboxHttpServletResponseWrapper((HttpServletResponse)response, renderPixel);

        try {
            chain.doFilter(requestWrapper, responseWrapper);
            if (responseWrapper.isBuffering()) {
                String snippet = null;

                if (renderPixel && localMachineID != null) {
                    snippet = "<iframe src=\"" + AuthboxConfig.endpoint + "/pixel?LMID=" + localMachineID + "\" width=\"0\" height=\"0\" style=\"border: none\" />";
                }

                String bufferedResponse = responseWrapper.getBufferedResponse(snippet);
                response.setContentLength(bufferedResponse.length());
                ((HttpServletResponse)response).addCookie(new Cookie(didGetPixelCookieName, "1"));

                out.write(bufferedResponse);
            }

            out.close();
        } finally {
            requestWrapper.logDefaultAction();
        }
    }
}
