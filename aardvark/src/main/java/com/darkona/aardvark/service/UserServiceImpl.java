package com.darkona.aardvark.service;

import com.darkona.aardvark.domain.Login;
import com.darkona.aardvark.domain.User;
import com.darkona.aardvark.exception.UserNotFoundException;
import com.darkona.aardvark.repository.UserRepository;
import com.darkona.aardvark.security.JwtGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private final JwtGenerator jwtGenerator;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, JwtGenerator jwtGenerator) {
        this.userRepository = userRepository;
        this.jwtGenerator = jwtGenerator;
        passwordEncoder = new BCryptPasswordEncoder();
    }

    public User signUserUp(User user) throws DataIntegrityViolationException {
        log.info("Received correctly formed user object.");
        Timestamp now = Timestamp.from(Instant.now());
        if (user.getName().isEmpty()) {
            user.setName("Anonymous");
        }
        log.info("Encrypting password...");
        user
                .setPassword(passwordEncoder.encode(user.getPlainPassword()))
                .setCreated(now)
                .setLastLogin(now)
                .setIsActive(true)
                .setEmail(user.getEmail().toLowerCase())
                .setToken(jwtGenerator.generateToken(user))
                .getPhones().forEach(phone -> phone.setUser(user));
        log.info("Saving to database...");
        return userRepository.saveAndFlush(user);
    }

    @Override
    public User getUserByLoginCredentials(Login login, Map<String, String> headers) throws UserNotFoundException, AuthenticationException {
        log.info("Token: {}", headers.get("bearer"));
        User user = userRepository.findUserByEmail(login.getEmail().toLowerCase()).orElseThrow(UserNotFoundException::new);
        if (!headers.get("bearer").equals(user.getToken())) {
            log.error("Incorrect token, throwing exception");
            throw new AuthenticationException("Incorrect token");
        }
        if (passwordEncoder.matches(login.getPassword(), user.getPassword())) {
            user
                    .setToken(jwtGenerator.generateToken(user))
                    .setLastLogin(Timestamp.from(Instant.now()));
            return userRepository.save(user).setPlainPassword(user.getPassword());
        } else {
            log.error("Wrong password, throwing exception");
            throw new AuthenticationException("Wrong password");
        }
    }

}
