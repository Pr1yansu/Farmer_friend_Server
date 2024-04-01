package com.priyansu.authentication.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.priyansu.authentication.entity.History;
import com.priyansu.authentication.entity.User;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReqRes {
    private int statusCode;
    private String error;
    private String message;
    private String token;
    private String refreshToken;
    private String expirationTime;
    private String phone_number;
    private String name;
    private String email;
    private String role;
    private String password;
    private String new_password;
    private List<History> history;
    private User user;
}
