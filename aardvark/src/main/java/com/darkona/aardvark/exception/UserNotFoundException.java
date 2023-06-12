package com.darkona.aardvark.exception;

public class UserNotFoundException extends Exception  {


    public UserNotFoundException(){
        super("User not found");
    }

}
