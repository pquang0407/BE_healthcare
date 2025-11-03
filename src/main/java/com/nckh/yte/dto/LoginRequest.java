package com.nckh.yte.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for login requests.  Supports both {@code username} and {@code account}
 * JSON property names for the username field to maintain compatibility with
 * older clients.  The {@code account} alias will be mapped to the
 * {@code username} property transparently by Jackson.
 */
@Getter
@Setter
public class LoginRequest {
    @JsonAlias("account")
    private String username;
    private String password;
}
