package com.immortals.miniurl.model.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private Collection<? extends GrantedAuthority> authorities;
}
