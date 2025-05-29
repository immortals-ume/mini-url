package com.immortals.miniurl.model.security;

import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {
    public User getCurrentUser() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        return (User) auth.getPrincipal();
        return null;
    }
}