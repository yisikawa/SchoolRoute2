package com.denso.geko.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.denso.geko.Constants.CommonConstants;
import com.denso.geko.domain.UserDomain;
import com.denso.geko.repository.UserRepository;

@Service
public class LoginUserDetailsServiceImpl implements LoginUserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        if (username == null || username.isEmpty()) {
            throw new UsernameNotFoundException("username is empty");
        }
        
        UserDomain user = userRepository.find(username);
        if (user == null || user.getPassword() == null) {
            throw new UsernameNotFoundException("username is not found");
        }
        
        List<GrantedAuthority> auth = new ArrayList<>();
        if (CommonConstants.ROLE_ADMIN.equals(user.getRole())) {
            auth = AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN");
        } else {
            auth = AuthorityUtils.createAuthorityList("ROLE_USER");
        }
        return new User(username, user.getPassword(), auth);
        
    }

}
