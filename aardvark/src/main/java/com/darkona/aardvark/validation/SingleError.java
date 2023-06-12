package com.darkona.aardvark.validation;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.Instant;


@Data
@NoArgsConstructor
public class SingleError {

    private Timestamp timestamp;
    private int codigo;
    private String detail;

    public SingleError(int codigo, Violation violation, String message){
        this.timestamp = Timestamp.from(Instant.now());
        this.codigo = codigo;
        this.detail = message + "[" + violation.getFieldname() + "] - " + violation.getMessage();
    }

    public SingleError(int codigo, Violation violation){

        this(codigo, violation, "Validation Error: ");
    }
}
