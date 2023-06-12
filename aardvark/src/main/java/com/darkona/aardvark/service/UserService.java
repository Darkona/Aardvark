package com.darkona.aardvark.service;

import com.darkona.aardvark.domain.Login;
import com.darkona.aardvark.domain.User;
import com.darkona.aardvark.exception.UserNotFoundException;

import javax.naming.AuthenticationException;
import java.util.Map;

public interface UserService {

    User signUserUp(User user);
    User getUserByLoginCredentials(Login login, Map<String, String> token) throws UserNotFoundException, AuthenticationException;

}
