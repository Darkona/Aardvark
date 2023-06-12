package com.darkona.aardvark.security;

import com.darkona.aardvark.domain.User;


public interface JwtGenerator {
    String generateToken(User user);

}
