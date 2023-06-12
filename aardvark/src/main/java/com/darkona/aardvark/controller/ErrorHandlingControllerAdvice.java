package com.darkona.aardvark.controller;

import com.darkona.aardvark.domain.User;
import com.darkona.aardvark.exception.UserNotFoundException;
import com.darkona.aardvark.validation.ErrorResponse;
import com.darkona.aardvark.validation.SingleError;
import com.darkona.aardvark.validation.Violation;
import com.github.JanLoebel.jsonschemavalidation.JsonSchemaValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.naming.AuthenticationException;
import javax.validation.ConstraintViolationException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.stream.Collectors;


@Component
@RestControllerAdvice
@Slf4j
public class ErrorHandlingControllerAdvice extends ResponseEntityExceptionHandler implements RequestBodyAdvice {

    private Object requestBody;
    @Value("${validation.jsonSchema}")
    private String JSON_SCHEMA_VAL;
    @Value("${validation.field.database}")
    private String DATABASE;
    @Value("${validation.field.internal}")
    private String INTERNAL;
    @Value("${validation.field.jsonSchema}")
    private String JSON_SCHEMA;
    @Value("${validation.field.authentication}")
    private String AUTHENTICATION;
    @Value("${validation.message.email: %s}")
    private String EMAIL_MESSAGE;
    @Value("${validation.message.database}")
    private String DATABASE_MESSAGE;
    @Value("${validation.message.authentication_error}")
    private String AUTHENTICATION_ERROR;


    private void warnTrigger(Exception e, String message){
        log.warn(e.getClass().getSimpleName() + "triggered. ErrorHandlingControllerAdvice handling it. Message: [" + message + "]");
    }

    @ResponseBody
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers, HttpStatus status, WebRequest request){
        log.warn("MethodArgumentNotValidException triggered");
        return new ResponseEntity<>(new ErrorResponse(e.getBindingResult().getFieldErrors()
                .stream()
                .map(p -> new SingleError(HttpStatus.BAD_REQUEST.value(), new Violation(p.getField(), p.getDefaultMessage())))
                .collect(Collectors.toList())), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JsonSchemaValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    protected ResponseEntity<Object> onJsonSchemaValidationException(JsonSchemaValidationException e) {
        log.warn("JsonSchemaValidationException triggered");
        ErrorResponse errorResponse = new ErrorResponse(e.getValidationMessages()
                .stream()
                .map(p ->  new SingleError(HttpStatus.BAD_REQUEST.value(), new Violation(JSON_SCHEMA, p.getMessage()), JSON_SCHEMA_VAL))
                .collect(Collectors.toList()));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    protected ResponseEntity<Object> onUserNotFoundException(UserNotFoundException e) {
        warnTrigger(e, "User not found");
        SingleError error = new SingleError(HttpStatus.NOT_FOUND.value(), new Violation(DATABASE, e.getMessage()));
        return new ResponseEntity<>(new ErrorResponse(Collections.singletonList(error)), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    protected ResponseEntity<Object> onConstraintValidationException(ConstraintViolationException e){
        warnTrigger(e, "Constraint violation detected");
        return new ResponseEntity<>(new ErrorResponse(e.getConstraintViolations().stream()
                .map(p -> new SingleError(HttpStatus.BAD_REQUEST.value(), new Violation(p.getPropertyPath().toString(), p.getMessage())))
                .collect(Collectors.toList())), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    protected ResponseEntity<Object> onAuthenticationException(AuthenticationException e){
        warnTrigger(e, e.getMessage());
        SingleError error = new SingleError(HttpStatus.UNAUTHORIZED.value(), new Violation(AUTHENTICATION, e.getMessage()), AUTHENTICATION_ERROR + ": ");
        ErrorResponse errorResponse = new ErrorResponse(Collections.singletonList(error));
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    protected ResponseEntity<Object> onDataIntegrityViolationException(DataIntegrityViolationException e){
        warnTrigger(e, "Account already exists");
        String email = ((User)requestBody).getEmail();
        String msg;
        if (e.getCause().getCause().getMessage().contains("Unique")){
            msg = "Account already exists";
        } else {
            msg = DATABASE_MESSAGE;
        }
        log.warn(msg + " Email: " + email);
        SingleError error = new SingleError(HttpStatus.CONFLICT.value(), new Violation(DATABASE, msg), INTERNAL);
        ErrorResponse errorResponse = new ErrorResponse(Collections.singletonList(error));
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) {
        return inputMessage;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        this.requestBody = body;
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

}
