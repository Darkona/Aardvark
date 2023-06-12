package com.darkona.aardvark.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.util.UUID;

@Data
@Entity
@Table(name = "PHONES")
@JsonPropertyOrder({"number", "citycode", "countrycode"})
//This is mere pedantry, by definition order of properties don't matter.
public class Phone {

    @ManyToOne
    @JoinColumn(name="USER_ID")
    @JsonIgnore //To avoid recursion in jackson deserialization :)
    @ToString.Exclude //To avoid recursion in string serialization
    private User user;

    @Id
    @GeneratedValue
    @Column(length = 16)
    @JsonIgnore
    private UUID id;

    private long number;

    private int citycode;

    @Pattern(regexp = "^(\\+\\d{2})$", message = "Must be a string containing the symbol '+' and exactly two digits.")
    private String countrycode;

}
