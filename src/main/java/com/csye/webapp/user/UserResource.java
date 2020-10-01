package com.csye.webapp.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@RestController
public class UserResource {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/v1/user/self")
    public String currentUserName(Authentication authentication) throws UserNotFoundException {
       User authenticatedUser = null;
        List<User> users = userRepository.findAll();
        for(User user : users){
            if(user.getUsername().equals(authentication.getName()))
            authenticatedUser = user;
        }
        if(authenticatedUser==null)
            throw new UserNotFoundException("id-" + authentication.getName());

       // authenticatedUser.setAccount_created( new Timestamp(System.currentTimeMillis()));
        return authenticatedUser.toString();
    }

//    @GetMapping("/users")
//    public List<User> retrieveAllUsers() {
//        return userRepository.findAll();
//    }

//    @GetMapping("/users/{id}")
//    public User retrieveUser(@PathVariable long id) throws UserNotFoundException {
//        Optional<User> user = userRepository.findById(id);
//
//        if (!user.isPresent())
//            throw new UserNotFoundException("id-" + id);
//
//        return user.get();
//    }

//    @DeleteMapping("/users/{id}")
//    public void deleteUser(@PathVariable long id) {
//        userRepository.deleteById(id);
//    }

    @PostMapping("/v1/user")
    public String createUser(@RequestBody User user) throws UserNotFoundException {
        if(!userRepository.findAll().isEmpty())
            for(User a : userRepository.findAll()){
                if (a.getUsername().equals(user.getUsername()))
                throw new UserNotFoundException("User already exists");
            }
        if(user.getPassword().length()<5){
            throw new UserNotFoundException("Password length should be more 5 ");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded_password = encoder.encode(user.getPassword());
        user.setPassword(encoded_password);
        user.setAccount_created( new Timestamp(System.currentTimeMillis()));
        user.setAccount_updated( new Timestamp(System.currentTimeMillis()));
        User savedUser = userRepository.save(user);

      //  URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
        //        .buildAndExpand(savedUser.getId()).toUri();

        return user.toString();

    }

    @PutMapping("/v1/user/self")
    public ResponseEntity<Object> updateUser(@RequestBody User user, Authentication authentication){
        if (!user.getUsername().equals(authentication.getName()))
            return ResponseEntity.badRequest().build();
        if(!user.getAccount_created().toString().isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        if(!user.getAccount_updated().toString().isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        List<User> users = userRepository.findAll();
        User authenticatedUser = null;
        for(User internalUser : users){
            if(internalUser.getUsername().equals(authentication.getName()))
                authenticatedUser = internalUser;
        }
        user.setId(authenticatedUser.getId());
        user.setAccount_created(authenticatedUser.getAccount_created());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if(user.getFirst_name().isEmpty()){
            user.setFirst_name(authenticatedUser.getFirst_name());
        }
        if(user.getLast_name().isEmpty()){
            user.setLast_name(authenticatedUser.getFirst_name());
        }
        if(!user.getPassword().isEmpty()) {
            String encoded_password = encoder.encode(user.getPassword());
            user.setPassword(encoded_password);
        }else{
            user.setPassword(authenticatedUser.getPassword());
        }
        user.setId(user.getId());

        user.setAccount_updated(new Timestamp(System.currentTimeMillis()));
        userRepository.save(user);

        return ResponseEntity.noContent().build();
    }

}
