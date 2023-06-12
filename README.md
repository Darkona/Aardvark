# 		/\ (ˆ(oo)ˆ) /\
# -AARDVARK-

## What is it
Aardvark is a demo project for using Java Spring Boot Framework with Spock Framework testing, implementing Java Security, JPA, H2 database and
validation.

Contract enforcement is done via JSON Schema validation, and Spring validation for data.
Password encryption with Spring Security in database.

Done as a test for GlobalLogic.

## What does it do

Create users in database, provide tokens, and allow login.

## Endpoints

* "/sign-up"
* "/login"
* "/check"

###  /Sign Up -- POST
Creates a user in database, provides JWT in return.

#### URL
When running locally, access via:
```http request
http://localhost:8080/sign-up
```

#### Contract
```json lines
{
  "name": String,
  "email": String,
  "password": String, //Plain text password
  "phones": [
    {
      "number": long,
      "citycode": int,
      "contrycode": String
    }
  ]
}
```

#### Response Contract
```json lines
{
  "id" : String, //UUID
  "name": String,
  "email": String,
  "password": String, //Encrypted password,
  "created" : String, //Formatted Timestamp - MMM dd, yyyy hh:mm:ss a
  "lastLogin" : String, //Formatted Timestamp - MMM dd, yyyy hh:mm:ss a]
  "isActive": Boolean,
  "token" : String, //JWT generated token]
  "phones": [
    {
      "number": Long,
      "citycode": Int,
      "contrycode": String
    }
  ]
}
```

#### Validation
##### Password 
 - Length between 8 and 12 characters.
 - Exactly one upper case letter.
 - Exactly two numbers, can be non-consecutive.
 - Only alphanumeric characters, no symbols.

**Name** and **Phones** are optional.
**Email** must be validated for proper format.

### /Login --POST
Attempts a login using email and password, adding the generated token.
Validates token and password and then shows the full data of the user.
Token is received via **header** with name **Bearer**

#### URL
When running locally, access via:
```http request
http://localhost:8080/login
```

#### Contract
```json lines
//Header 
[{
  "Bearer" : "JWT Token generated at sign-up"
}]
//Body
{
  "email": String,
  "password": String, //Plain text password
}
```

#### Response
```json lines
{
  "id" : String, //UUID
  "name": String,
  "email": String,
  "password": String, //Encrypted password,
  "created" : String, //Formatted Timestamp - MMM dd, yyyy hh:mm:ss a
  "lastLogin" : String, //**UPDATED** Formatted Timestamp - MMM dd, yyyy hh:mm:ss a]
  "isActive": Boolean,
  "token" : String, // **New** JWT generated token
  "phones": [
    {
      "number": Long,
      "citycode": Int,
      "contrycode": String
    }
  ]
}
```

#### /Check -- GET

A simple health check for the application running.

#### URL
When running locally, access via:
```
http://localhost:8080/
```
#### Response
```text
"ok"
```

## Changes

* 1.0.0 Initial version

##### Build with Gradle
```shell
gradlew build bootRun
```
