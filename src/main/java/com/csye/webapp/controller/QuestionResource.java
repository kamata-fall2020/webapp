package com.csye.webapp.controller;

import com.csye.webapp.exception.ImproperException;
import com.csye.webapp.exception.UnauthorizedException;
import com.csye.webapp.exception.UserNotFoundException;
import com.csye.webapp.model.Category;
import com.csye.webapp.model.Files;
import com.csye.webapp.model.Question;
import com.csye.webapp.model.User;
import com.csye.webapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.*;

@RestController
public class QuestionResource {

        @Autowired
        private UserRepository userRepository;

    @Autowired
    private AWSS3Service service;


    @Autowired
    private FileRepository fileRepository;

        @Autowired
        private QuestionRepository questionRepository;


        @Autowired
        private AnswerRepository answerRepository;

        @Autowired
        private CategoryRepository categoryRepository;

        @PostMapping("/v1/question")
        public Question postQuestion(@Valid @RequestBody Question question, Authentication authentication) {
            List<User> users = userRepository.findAll();
            User authenticatedUser = null;
            for (User internalUser : users) {
                if (internalUser.getUsername().equals(authentication.getName()))
                    authenticatedUser = internalUser;
            }
            if(authenticatedUser==null){
                throw new UnauthorizedException("User is not authenticated");
            }
            question.setUser_id(authenticatedUser.getUser_id());
           Set<Category> categoriesFromRequestBody = question.getCategories();
           question.setCategories(null);
           List<Category> categoriesFromDB = categoryRepository.findAll();
           Set<Category> newCategories = new HashSet<>();

            if(!categoriesFromDB.isEmpty()){
               if(!categoriesFromRequestBody.isEmpty())
               {
                       for (Category cRB : categoriesFromRequestBody){
                           int flag = 0;
                               for(Category cDB : categoriesFromDB ) {
                                   if (cDB.getCategory().trim().toLowerCase().equals(cRB.getCategory().trim().toLowerCase())) {
                                     flag =1;
                                       newCategories.add(cDB);
                                   }
                               }
                           if (flag != 1) {
                           //    System.out.println("saving "+cRB.getCategory());
                               if(cRB.getCategory().contains("@") || cRB.getCategory().contains("#")
                                       || cRB.getCategory().contains("!") || cRB.getCategory().contains("~")
                                       || cRB.getCategory().contains("$") || cRB.getCategory().contains("%")
                                       || cRB.getCategory().contains("^") || cRB.getCategory().contains("&")
                                       || cRB.getCategory().contains("*") || cRB.getCategory().contains("(")
                                       || cRB.getCategory().contains(")") || cRB.getCategory().contains("-")
                                       || cRB.getCategory().contains("+") || cRB.getCategory().contains("/")
                                       || cRB.getCategory().contains(":") || cRB.getCategory().contains(".")
                                       || cRB.getCategory().contains(", ") || cRB.getCategory().contains("<")
                                       || cRB.getCategory().contains(">") || cRB.getCategory().contains("?")
                                       || cRB.getCategory().contains("|")){
                                   throw new ImproperException("Category should not have special character");
                               }
                               categoryRepository.save(cRB);
                               newCategories.add(cRB);
                           }
                       }
               }
           }else{
             //  System.out.println("++++++++++++++++++++++++++++ inside else");
               question.setCategories(categoriesFromRequestBody);
               for (Category c : categoriesFromRequestBody) {
                   if(c.getCategory().contains("@") || c.getCategory().contains("#")
                           || c.getCategory().contains("!") || c.getCategory().contains("~")
                           || c.getCategory().contains("$") || c.getCategory().contains("%")
                           || c.getCategory().contains("^") || c.getCategory().contains("&")
                           || c.getCategory().contains("*") || c.getCategory().contains("(")
                           || c.getCategory().contains(")") || c.getCategory().contains("-")
                           || c.getCategory().contains("+") || c.getCategory().contains("/")
                           || c.getCategory().contains(":") || c.getCategory().contains(".")
                           || c.getCategory().contains(", ") || c.getCategory().contains("<")
                           || c.getCategory().contains(">") || c.getCategory().contains("?")
                           || c.getCategory().contains("|")){
                       throw new ImproperException("Category should not have special character");
                   }
                   categoryRepository.save(c);
               }
           }

            question.setQuestion_created(new Timestamp(System.currentTimeMillis()));
            question.setQuestion_updated(new Timestamp(System.currentTimeMillis()));
            if(!newCategories.isEmpty()) {
                question.setCategories(newCategories);
            }
            questionRepository.save(question);


            return question;
        }

    // Retrieve question details as per question_id
    @GetMapping("/v1/question/{question_id}")
    public Question retrieveUser(@PathVariable String question_id) throws UserNotFoundException {
        Optional<Question> question = questionRepository.findById(question_id);

        if (!question.isPresent())
            throw new UserNotFoundException("Question id not found" + question_id);

        return question.get();
    }

        @DeleteMapping("/v1/question/{question_id}")
       public ResponseEntity<Object> deleteQuestion(@PathVariable String question_id, Authentication authentication) {


            List<User> users = userRepository.findAll();
            User authenticatedUser = null;
            for (User internalUser : users) {
                if (internalUser.getUsername().equals(authentication.getName()))
                    authenticatedUser = internalUser;
            }
            if(authenticatedUser==null){
                throw new UnauthorizedException("user not found");
            }

            Optional<Question> question = questionRepository.findById(question_id);

            if (!question.isPresent())
                throw new UserNotFoundException("Question not found" + question_id);

            if(!question.get().getUser_id().equals(authenticatedUser.getUser_id())){
                throw new UnauthorizedException("User has not written this question");
            }

            if(!question.get().getAnswerList().isEmpty()){
                throw new ImproperException("Question has answer so cannot delete");
            }

            List<Files> file = fileRepository.findAll();
            for (Files files : file) {
                if (files.getQuestion_id().equals(question_id))
                   // authenticatedUser = internalUser;
                service.deleteFile(files.getS3_object_name());
            }
        questionRepository.deleteById(question_id);
            return ResponseEntity.noContent().build();
    }


        @GetMapping("v1/questions")
        public List<Question> retrieveAllUsers() throws UserNotFoundException {
            List<Question> questions = questionRepository.findAll();
            if(questions.isEmpty()){
                throw new  UserNotFoundException("Questions Not Found");
            }

            return questionRepository.findAll();
    }

    @PutMapping("/v1/question/{question_id}")
    public ResponseEntity<Object> updateQuestion(@PathVariable String question_id, @RequestBody Question question, Authentication authentication){
        List<User> users = userRepository.findAll();
        User authenticatedUser = null;
        for(User internalUser : users){
            if(internalUser.getUsername().equals(authentication.getName()))
                authenticatedUser = internalUser;
        }
        if(authenticatedUser==null){
            throw new UnauthorizedException("user not found");
        }
        Optional<Question> questionById = questionRepository.findById(question_id);
        if(!questionById.isPresent()){
            throw new UserNotFoundException("question not found");
        }
        if(!questionById.get().getUser_id().equals(authenticatedUser.getUser_id())){
            throw new UnauthorizedException(" question does not belong to User");
        }
        questionById.get().setQuestion_id(question_id);
        questionById.get().setQuestion_text(question.getQuestion_text());
        questionById.get().setQuestion_updated(new Timestamp(System.currentTimeMillis()));
        questionById.get().setCategories(question.getCategories());
        questionRepository.save(questionById.get());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/v1/question/{question_id}/file")
    public Files uploadFile(@PathVariable String question_id, Authentication authentication, @RequestPart(value = "file") MultipartFile fileInput) {
        List<User> users = userRepository.findAll();
        Files file = new Files();
        User authenticatedUser = null;
        for(User internalUser : users){
            if(internalUser.getUsername().equals(authentication.getName()))
                authenticatedUser = internalUser;
        }
        if(authenticatedUser==null){
            throw new UnauthorizedException("user not found");
        }
        Optional<Question> questionById = questionRepository.findById(question_id);
        if(!questionById.isPresent()){
            throw new UserNotFoundException("question not found");
        }
        if(!questionById.get().getUser_id().equals(authenticatedUser.getUser_id())){
            throw new UnauthorizedException(" question does not belong to User");
        }

        String file_name ="";
        Timestamp date = new Timestamp(System.currentTimeMillis());

            try {

                if (fileInput.getOriginalFilename().isEmpty()){
                    throw new UserNotFoundException("file is not present");
                }
                file_name = question_id+"/"+date.toString()+"/"+fileInput.getOriginalFilename().replace(" ", "_");
           service.uploadFile(file_name,fileInput);

        } catch (Exception e) {

            throw new ImproperException("Some issue while processing file");
        }
        file.setQuestion_id(question_id);
        file.setCreated_Date(date);
        file.setFile_name(fileInput.getOriginalFilename().replace(" ", "_"));
        file.setSize(fileInput.getSize());
        file.setContentType(fileInput.getContentType());
        file.setS3_object_name(file_name);
        fileRepository.save(file);

    return file;
    }


    @DeleteMapping("/v1/question/{question_id}/file/{file_id}")
    public ResponseEntity<Object> deleteFile(@PathVariable String question_id,@PathVariable String file_id,Authentication authentication) {


        List<User> users = userRepository.findAll();
        User authenticatedUser = null;
        for (User internalUser : users) {
            if (internalUser.getUsername().equals(authentication.getName()))
                authenticatedUser = internalUser;
        }
        if(authenticatedUser==null){
            throw new UnauthorizedException("user not found");
        }

        Optional<Question> question = questionRepository.findById(question_id);

        if (!question.isPresent())
            throw new UserNotFoundException("Question not found" + question_id);

        if(!question.get().getUser_id().equals(authenticatedUser.getUser_id())){
            throw new UnauthorizedException("User has not written this question");
        }

        Optional<Files> file = fileRepository.findById(file_id);

        if (!file.isPresent())
            throw new UserNotFoundException("file not found" + question_id);
        if(!file.get().getQuestion_id().equals(question_id))
            throw new UnauthorizedException("file does not belong to question");

        service.deleteFile(file.get().getS3_object_name());
        fileRepository.deleteById(file.get().getFile_id());
        return ResponseEntity.noContent().build();
    }






}
