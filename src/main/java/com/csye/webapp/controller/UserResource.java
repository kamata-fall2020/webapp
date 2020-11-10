package com.csye.webapp.controller;


import com.csye.webapp.exception.ImproperException;
import com.csye.webapp.exception.UnauthorizedException;
import com.csye.webapp.exception.UserNotFoundException;
import com.csye.webapp.model.User;
import com.csye.webapp.repository.AnswerRepository;
import com.csye.webapp.repository.CategoryRepository;
import com.csye.webapp.repository.QuestionRepository;
import com.csye.webapp.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.timgroup.statsd.StatsDClient;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@RestController
public class UserResource {

    private final static Logger logger = LoggerFactory.getLogger(UserResource.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    StatsDClient statsDClient;

    @GetMapping("/v1/user/self")
    public User currentUserName(Authentication authentication) throws UserNotFoundException {
        logger.info("GET Request for Self User ");

        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("endpoint.user.self.http.get");

       User authenticatedUser = null;
        List<User> users = userRepository.findAll();
        for(User user : users){
            if(user.getUsername().equals(authentication.getName()))
            authenticatedUser = user;
        }
        if(authenticatedUser==null)
            throw new UnauthorizedException("id-" + authentication.getName());


       // authenticatedUser.setAccount_created( new Timestamp(System.currentTimeMillis()));
        long end = System.currentTimeMillis();
        long result = end-start;
        statsDClient.recordExecutionTime("timer.user.self.get",result);
        return authenticatedUser;
    }

//    @GetMapping("/users")
//    public List<User> retrieveAllUsers() {
//        return userRepository.findAll();
//    }

    @GetMapping("/users/{id}")
    public User retrieveUser(@PathVariable String id) throws UserNotFoundException {
        logger.info("GET Request for particular User ");

        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("endpoint.user.id.http.get");

        Optional<User> user = userRepository.findById(id);

        if (!user.isPresent())
            throw new UserNotFoundException("id-" + id);


        long end = System.currentTimeMillis();
        long result = end-start;
        statsDClient.recordExecutionTime("timer.user.id.get",result);



        return user.get();
    }

//    @DeleteMapping("/users/{id}")
//    public void deleteUser(@PathVariable long id) {
//        userRepository.deleteById(id);
//    }

    @PostMapping("/v1/user")
    public User createUser( @Valid @RequestBody User user) throws UserNotFoundException {
        logger.info("POST Request for Create User ");
        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("endpoint.user.http.post");

        if(!userRepository.findAll().isEmpty())
            for(User a : userRepository.findAll()){
                if (a.getUsername().equals(user.getUsername()))
                throw new ImproperException("User already exists");
            }
        if(!isValid(user.getPassword())){
            throw new ImproperException("Password should not contain any space.\n" +
                    "Password should contain at least one digit(0-9).\n" +
                    "Password length should be between 8 to 15 characters.\n" +
                    "Password should contain at least one lowercase letter(a-z).\n" +
                    "Password should contain at least one uppercase letter(A-Z).\n" +
                    "Password should contain at least one special character ( @, #, %, &, !, $, etc….). ");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded_password = encoder.encode(user.getPassword());
        user.setPassword(encoded_password);
        user.setEnabled(1);
        user.setAccount_created( new Timestamp(System.currentTimeMillis()));
        user.setAccount_updated( new Timestamp(System.currentTimeMillis()));
        userRepository.save(user);

        long end = System.currentTimeMillis();
        long result = end-start;
        statsDClient.recordExecutionTime("timer.user.post",result);

        return user;

    }

    @PutMapping("/v1/user/self")
    public ResponseEntity<Object> updateUser(@Valid @RequestBody User user, Authentication authentication){
        logger.info("PUT Request for Update User ");

        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("endpoint.user.http.put");

        if (!user.getUsername().equals(authentication.getName()))
            throw new UnauthorizedException("User is not authenticated");

        List<User> users = userRepository.findAll();
       //
        for(User internalUser : users){
            if(internalUser.getUsername().equals(authentication.getName()))
                user.setUser_id(internalUser.getUser_id());
                user.setAccount_created(internalUser.getAccount_created());
                user.setEnabled(1);
        }


        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        if(!isValid(user.getPassword())){
            throw new ImproperException("Password should not contain any space.\n" +
                    "Password should contain at least one digit(0-9).\n" +
                    "Password length should be between 8 to 15 characters.\n" +
                    "Password should contain at least one lowercase letter(a-z).\n" +
                    "Password should contain at least one uppercase letter(A-Z).\n" +
                    "Password should contain at least one special character ( @, #, %, &, !, $, etc….). ");

        }
        String encoded_password = encoder.encode(user.getPassword());
        user.setPassword(encoded_password);
       // user.setUser_id(user.getUser_id());
        user.setAccount_updated(new Timestamp(System.currentTimeMillis()));
        userRepository.save(user);

        long end = System.currentTimeMillis();
        long result = end-start;
        statsDClient.recordExecutionTime("timer.user.put",result);


        return ResponseEntity.noContent().build();
    }


    public  boolean isValid(String password)
    {

        // for checking if password length
        // is between 8 and 15
        if (!((password.length() >= 8)
                && (password.length() <= 15))) {
            return false;
        }

        // to check space
        if (password.contains(" ")) {
            return false;
        }
        if (true) {
            int count = 0;

            // check digits from 0 to 9
            for (int i = 0; i <= 9; i++) {

                // to convert int to string
                String str1 = Integer.toString(i);

                if (password.contains(str1)) {
                    count = 1;
                }
            }
            if (count == 0) {
                return false;
            }
        }

        // for special characters
        if (!(password.contains("@") || password.contains("#")
                || password.contains("!") || password.contains("~")
                || password.contains("$") || password.contains("%")
                || password.contains("^") || password.contains("&")
                || password.contains("*") || password.contains("(")
                || password.contains(")") || password.contains("-")
                || password.contains("+") || password.contains("/")
                || password.contains(":") || password.contains(".")
                || password.contains(", ") || password.contains("<")
                || password.contains(">") || password.contains("?")
                || password.contains("|"))) {
            return false;
        }

        if (true) {
            int count = 0;

            // checking capital letters
            for (int i = 65; i <= 90; i++) {

                // type casting
                char c = (char)i;

                String str1 = Character.toString(c);
                if (password.contains(str1)) {
                    count = 1;
                }
            }
            if (count == 0) {
                return false;
            }
        }

        if (true) {
            int count = 0;

            // checking small letters
            for (int i = 90; i <= 122; i++) {

                // type casting
                char c = (char)i;
                String str1 = Character.toString(c);

                if (password.contains(str1)) {
                    count = 1;
                }
            }
            if (count == 0) {
                return false;
            }
        }

        // if all conditions fails
        return true;
    }



}
