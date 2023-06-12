package com.darkona.aardvark.controller;

import com.darkona.aardvark.domain.Login;
import com.darkona.aardvark.domain.User;
import com.darkona.aardvark.exception.UserNotFoundException;
import com.darkona.aardvark.service.UserService;
import com.github.JanLoebel.jsonschemavalidation.JsonSchemaValidation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    @Value("${user_request.schema.location}")
    static final String USER_SCHEMA = "classpath:model/user_request.schema.json";

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/check")
    public ResponseEntity<String> check(){
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @GetMapping(value = "/home", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String,String>> home(){
        Map<String, String> healthcheck = new HashMap<>();
        healthcheck.put("Timestamp", Timestamp.from(Instant.now()).toString());
        healthcheck.put("Message", "Works ok!");
        return new ResponseEntity<>( healthcheck, HttpStatus.OK);
    }


    @PostMapping(value = "/sign-up", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> signUp(@Valid @JsonSchemaValidation(USER_SCHEMA) @RequestBody User user) {
        log.info("Receiving request: {}", user);
        return new ResponseEntity<>(userService.signUserUp(user), HttpStatus.CREATED);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> login(@Valid @RequestBody Login login, @RequestHeader Map<String, String> headers) throws UserNotFoundException, AuthenticationException {

        log.info("Attempting login with request: email: {} and password: *********", login.getEmail());

        log.info("Headers: {}", headers.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining(",")));
        if(!headers.containsKey("bearer")) {
            throw new AuthenticationException("No token.");
        }

        User user = userService.getUserByLoginCredentials(login, headers);

        String token = user.getToken();
        user.setToken(token.substring(0,44)+"...");

        HttpHeaders newHeaders = new HttpHeaders();
        newHeaders.put("token", Collections.singletonList(token));

        return new ResponseEntity<>(user, newHeaders, HttpStatus.OK);
    }


}
