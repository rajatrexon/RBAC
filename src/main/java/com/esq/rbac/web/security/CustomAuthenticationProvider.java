package com.esq.rbac.web.security;

import com.esq.rbac.web.client.UserDetailsRBAC;
import com.esq.rbac.web.client.UserDetailsService;
import com.esq.rbac.web.util.EncryptionUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final com.esq.rbac.web.client.UserDetailsService userDetailsService;

    public CustomAuthenticationProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        return getAuthentication(username,password);
    }



public  Authentication getAuthentication(String username,String password) {

    UserDetailsRBAC userDetails = userDetailsService.loadUserByUsername(username);


  return  new UsernamePasswordAuthenticationToken(userDetails.getUsername(),password,new ArrayList<>());
}







    @Override
    public boolean supports(Class<?> authentication) {
        // TODO Auto-generated method stub
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }


}

