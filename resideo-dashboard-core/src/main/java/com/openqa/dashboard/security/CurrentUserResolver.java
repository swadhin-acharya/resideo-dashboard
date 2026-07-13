package com.openqa.dashboard.security;

import com.openqa.dashboard.model.entity.UserEntity;
import com.openqa.dashboard.repository.UserRepository;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

@Component
public class CurrentUserResolver implements HandlerMethodArgumentResolver {

    private final UserRepository userRepository;

    public CurrentUserResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && (parameter.getParameterType().equals(UserEntity.class)
                    || parameter.getParameterType().equals(UUID.class));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return parameter.getParameterType().equals(UUID.class) ? null : null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof DashboardUserDetails userDetails) {
            if (parameter.getParameterType().equals(UUID.class)) {
                return userDetails.getUserId();
            }
            return userRepository.findById(userDetails.getUserId()).orElse(null);
        }
        return null;
    }
}
