package com.darkona.aardvark.util;

import com.darkona.aardvark.domain.Phone;
import com.darkona.aardvark.domain.User;
import com.darkona.aardvark.validation.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;

public class MapperUtil {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
    private final ObjectMapper objectMapper = new ObjectMapper().setDateFormat(dateFormat);

    public User deserializeUser(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, User.class);
    }

    public Phone deserializePhone(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, Phone.class);
    }

    public User deserializeResponseUser(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, User.class);
    }

    public ErrorResponse deserializeErrorResponse(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, ErrorResponse.class);
    }
}
