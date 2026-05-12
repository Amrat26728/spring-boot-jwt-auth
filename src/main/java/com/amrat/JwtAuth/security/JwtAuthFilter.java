package com.amrat.JwtAuth.security;

import com.amrat.JwtAuth.entity.User;
import com.amrat.JwtAuth.service.UserService;
import com.amrat.JwtAuth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    private final HandlerExceptionResolver handlerExceptionResolver;

    // register this filter in the WebSecurityConfig file
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            final String requestTokenHeader = request.getHeader("Authorization");

            if(requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer")){
                filterChain.doFilter(request, response);
                return;
            }

            String token = requestTokenHeader.split("Bearer ")[1];

            String username = jwtUtil.getUsernameFromToken(token);

            // username should not be null and security context should be null
            // every filter has to fill security context
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null){
                User user = userService.findByUsername(username);

                System.out.println(user.getUsername());

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }

            filterChain.doFilter(request, response);
        } catch (Exception ex){
            // this sends exception to global exception handler
            System.out.println("Exception: "+ex.toString());
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }
}
