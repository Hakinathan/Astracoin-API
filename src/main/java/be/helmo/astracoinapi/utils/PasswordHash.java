package be.helmo.astracoinapi.utils;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class PasswordHash {
    private final String salt = "$2a$10$5od4oWvUD72FZgArWG3cGe";

    public PasswordHash(){

    }

    public String hash(String txt){
        return BCrypt.hashpw(txt, salt);

    }
}
