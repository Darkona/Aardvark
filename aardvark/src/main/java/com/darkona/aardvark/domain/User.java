package com.darkona.aardvark.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "USERS")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "created", "lastLogin", "token", "isActive", "name", "email", "password", "phones"})
public class User {

    @Id
    @GeneratedValue
    @Column(length = 16) //Fix for https://github.com/h2database/h2database/issues/3523
    private UUID id;

    private Timestamp created;

    private Timestamp lastLogin;

    private String name;

    private Boolean isActive;

    @Column(length = 512)
    private String token;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY, value = "password")
    @Pattern.List({
            @Pattern( regexp = "^.+$", message = "{validation.password.empty}"),
            @Pattern( regexp = "^.{8,12}$", message = "{validation.password.length}"),
            @Pattern( regexp = "^(?=(\\D*\\d\\D*){2})(?!(\\D*\\d\\D*){3})[a-zA-Z\\d]+$" , message = "{validation.password.number}"),
            @Pattern( regexp = "^(?!([^A-Z]*[A-Z][^A-Z]*){2})(?=([^A-Z]*[A-Z][^A-Z]*){1})[a-zA-Z\\d]+$", message = "{validation.password.upper}"),
            @Pattern( regexp = "^[a-zA-Z\\d]*$", message = "{validation.password.alpha}")
    })
    /*@Pattern(regexp = "^(?!(\\D*\\d\\D*){3})(?=(\\D*\\d\\D*){2})(?!([^A-Z]*[A-Z][^A-Z]*){2})[a-zA-Z\\d]{8,12}$", message =
            "Password must be 8-12 alphanumeric characters long, with exactly one upper case letter and exactly two digits.")*/
    private String plainPassword;

    @Column(name="password")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY, value = "password")
    private String password;

    @Email(message = "{validation.email}")
    @Column(unique = true)
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<@Valid Phone> phones;
}
