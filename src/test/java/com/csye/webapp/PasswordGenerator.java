package com.csye.webapp;

import com.mysql.cj.conf.PropertyDefinitions;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args){

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "password";
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println(encodedPassword);

    }
}
