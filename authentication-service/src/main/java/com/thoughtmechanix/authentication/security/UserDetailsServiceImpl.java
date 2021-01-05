package com.thoughtmechanix.authentication.security;

import com.thoughtmechanix.authentication.model.User;
import com.thoughtmechanix.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 用户登录认证信息查询
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired
  private UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUserName(username);
    if (user == null) {
      throw new UsernameNotFoundException("该用户不存在");
    }

    return new JwtUserDetails(user.getUserName(), user.getPassword(), null);
  }
}
