package com.darkona.aardvark.validation;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class ErrorResponse {

    private final List<SingleError> error = new ArrayList<>();

    public ErrorResponse(List<SingleError> errors) {
        error.addAll(errors);
    }


}
