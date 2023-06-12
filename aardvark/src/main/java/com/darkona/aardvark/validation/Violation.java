package com.darkona.aardvark.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class Violation {

    private final String fieldname;
    private final String message;

}
