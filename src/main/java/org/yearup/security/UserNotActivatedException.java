package org.yearup.security;

import org.springframework.security.core.AuthenticationException;

import java.io.Serial;

public class UserNotActivatedException extends AuthenticationException {

    @Serial
    private static final long serialVersionUID = -1126699074574529145L;

    public UserNotActivatedException(String message) {
        super(message);
    }
}
