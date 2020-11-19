package com.csye.webapp.controller;

import com.csye.webapp.exception.ImproperException;
import com.csye.webapp.exception.UnauthorizedException;
import com.csye.webapp.exception.UserNotFoundException;
import com.csye.webapp.model.Category;
import com.csye.webapp.model.Files;
import com.csye.webapp.model.Question;
import com.csye.webapp.model.User;
import com.csye.webapp.repository.*;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final static Logger logger = LoggerFactory.getLogger(UserResource.class);

    @Autowired
    StatsDClient statsDClient;

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

            logger.info("POST Request for Create Question ");

            long start = System.currentTimeMillis();
            statsDClient.incrementCounter("endpoint.question.http.post");

            List<User> users = userRepository.findAll();
            User authenticatedUser = null;
            for (User internalUser : users) {
                if (internalUser.getUsername().equals(authentication.getName()))
                    authenticatedUser = internalUser;
            }
            if(authenticatedUser==null){
                logger.info("POST Request for Create Question didnt work as user is not authenticated "+authentication.getName());
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
                                   logger.info("Category should not have special character");
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
                       logger.info("Category should not have special character");
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
            long startD = System.currentTimeMillis();
            questionRepository.save(question);
            long endD = System.currentTimeMillis();
            long resultD = endD-startD;
            statsDClient.recordExecutionTime("timer.question.database.post",resultD);

            long end = System.currentTimeMillis();
            long result = end-start;
            statsDClient.recordExecutionTime("timer.question.post",result);


            return question;
        }

    // Retrieve question details as per question_id
    @GetMapping("/v1/question/{question_id}")
    public Question retrieveUser(@PathVariable String question_id) throws UserNotFoundException {
        logger.info("GET Request for  Question by ID ");

        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("endpoint.question.http.get");
        Optional<Question> question = questionRepository.findById(question_id);

        if (!question.isPresent()) {
            logger.info("GET Request for  Question didnt work as questionid not found "+ question_id);
            throw new UserNotFoundException("Question id not found" + question_id);
        }

        long end = System.currentTimeMillis();
        long result = end-start;
        statsDClient.recordExecutionTime("timer.question.get",result);

        return question.get();
    }

        @DeleteMapping("/v1/question/{question_id}")
       public ResponseEntity<Object> deleteQuestion(@PathVariable String question_id, Authentication authentication) { logger.info("POST Request for Create Question ");
            logger.info("Delete Request for  Question by ID ");
            long start = System.currentTimeMillis();
            statsDClient.incrementCounter("endpoint.question.http.delete");

            List<User> users = userRepository.findAll();
            User authenticatedUser = null;
            for (User internalUser : users) {
                if (internalUser.getUsername().equals(authentication.getName()))
                    authenticatedUser = internalUser;
            }
            if(authenticatedUser==null){
                logger.info("Delete Request for  Question didnt work as user not found "+ authentication.getName());
                throw new UnauthorizedException("user not found");
            }

            Optional<Question> question = questionRepository.findById(question_id);

            if (!question.isPresent()) {
                logger.info("Delete Request for  Question didnt work as question not found "+ question_id);
                throw new UserNotFoundException("Question not found" + question_id);
            }

            if(!question.get().getUser_id().equals(authenticatedUser.getUser_id())){
                logger.info("Delete Request for  Question didnt work as user not formed this question "+ question_id);
                throw new UnauthorizedException("User has not written this question");
            }

            if(!question.get().getAnswerList().isEmpty()){
                logger.info("Delete Request for  Question didnt work as it has answer ");
                throw new ImproperException("Question has answer so cannot delete");
            }

            List<Files> file = fileRepository.findAll();
            for (Files files : file) {
                if (files.getQuestion_id().equals(question_id)) {
                    // authenticatedUser = internalUser;
                    long startS3 = System.currentTimeMillis();
                    service.deleteFile(files.getS3_object_name());
                    long endS3 = System.currentTimeMillis();
                    long resultS3 = endS3 - startS3;
                    statsDClient.recordExecutionTime("timer.question.delete.S3Service", resultS3);
                }
            }
            long startD = System.currentTimeMillis();
        questionRepository.deleteById(question_id);
            long endD = System.currentTimeMillis();
            long resultD = endD-startD;
            statsDClient.recordExecutionTime("timer.question.database.delete",resultD);


            long end = System.currentTimeMillis();
            long result = end-start;
            statsDClient.recordExecutionTime("timer.question.delete",result);
            return ResponseEntity.noContent().build();
    }


        @GetMapping("v1/questions")
        public List<Question> retrieveAllUsers() throws UserNotFoundException {

            logger.info("Get Request for all Questions");
            long start = System.currentTimeMillis();
            statsDClient.incrementCounter("endpoint.question.all.http.get");


            List<Question> questions = questionRepository.findAll();
            if(questions.isEmpty()){
                throw new  UserNotFoundException("Questions Not Found");
            }


            long end = System.currentTimeMillis();
            long result = end-start;
            statsDClient.recordExecutionTime("timer.question.all.get",result);
            return questionRepository.findAll();
    }

    @PutMapping("/v1/question/{question_id}")
    public ResponseEntity<Object> updateQuestion(@PathVariable String question_id, @RequestBody Question question, Authentication authentication){

        logger.info("Get Request for all Questions");
        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("endpoint.question.http.put");

            List<User> users = userRepository.findAll();
        User authenticatedUser = null;
        for(User internalUser : users){
            if(internalUser.getUsername().equals(authentication.getName()))
                authenticatedUser = internalUser;
        }
        if(authenticatedUser==null){
            logger.info("Get Request for all Questions didnt work as user not found "+authentication.getName());
            throw new UnauthorizedException("user not found");
        }
        Optional<Question> questionById = questionRepository.findById(question_id);
        if(!questionById.isPresent()){
            logger.info("Get Request for all Questions didnt work as question not found "+ question_id);
            throw new UserNotFoundException("question not found");
        }
        if(!questionById.get().getUser_id().equals(authenticatedUser.getUser_id())){
            logger.info("Get Request for all Questions didnt work as question not formed by user "+ question_id);
            throw new UnauthorizedException(" question does not belong to User");
        }
        questionById.get().setQuestion_id(question_id);
        questionById.get().setQuestion_text(question.getQuestion_text());
        questionById.get().setQuestion_updated(new Timestamp(System.currentTimeMillis()));
        questionById.get().setCategories(question.getCategories());
        long startD = System.currentTimeMillis();
        questionRepository.save(questionById.get());
        long endD = System.currentTimeMillis();
        long resultD = endD-startD;
        statsDClient.recordExecutionTime("timer.question.database.put",resultD);

        long end = System.currentTimeMillis();
        long result = end-start;
        statsDClient.recordExecutionTime("timer.question.put",result);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/v1/question/{question_id}/file")
    public Files uploadFile(@PathVariable String question_id, Authentication authentication, @RequestPart(value = "file") MultipartFile fileInput) {

        logger.info("Post Request for create File in Questions");
        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("endpoint.question.file.post");

            List<User> users = userRepository.findAll();
        Files file = new Files();
        User authenticatedUser = null;
        for(User internalUser : users){
            if(internalUser.getUsername().equals(authentication.getName()))
                authenticatedUser = internalUser;
        }
        if(authenticatedUser==null){
            logger.info("Post Request for create File in Questions didnt work as user not found");
            throw new UnauthorizedException("user not found");
        }
        Optional<Question> questionById = questionRepository.findById(question_id);
        if(!questionById.isPresent()){
            logger.info("Post Request for create File in Questions didnt work as question not found");
            throw new UserNotFoundException("question not found");
        }
        if(!questionById.get().getUser_id().equals(authenticatedUser.getUser_id())){
            logger.info("Post Request for create File in Questions didnt work as question not formed by user");
            throw new UnauthorizedException(" question does not belong to User");
        }

        String file_name ="";
        Timestamp date = new Timestamp(System.currentTimeMillis());

            try {

                if (fileInput.getOriginalFilename().isEmpty()){
                    throw new UserNotFoundException("file is not present");
                }
                file_name = question_id+"/"+date.toString()+"/"+fileInput.getOriginalFilename().replace(" ", "_");
                long startS3 = System.currentTimeMillis();
           service.uploadFile(file_name,fileInput);
                long endS3 = System.currentTimeMillis();
                long resultS3 = endS3-startS3;
                statsDClient.recordExecutionTime("timer.question.file.post.S3Service",resultS3);

        } catch (Exception e) {

            throw new ImproperException("Some issue while processing file " + e);
        }
        file.setQuestion_id(question_id);
        file.setCreated_Date(date);
        file.setFile_name(fileInput.getOriginalFilename().replace(" ", "_"));
        file.setSize(fileInput.getSize());
        file.setContentType(fileInput.getContentType());
        file.setS3_object_name(file_name);
        long startD = System.currentTimeMillis();
        fileRepository.save(file);
        long endD = System.currentTimeMillis();

        long resultD = endD-startD;
        statsDClient.recordExecutionTime("timer.question.database.file.post",resultD);
        long end = System.currentTimeMillis();
        long result = end-start;
        statsDClient.recordExecutionTime("timer.question.file.post",result);
    return file;
    }


    @DeleteMapping("/v1/question/{question_id}/file/{file_id}")
    public ResponseEntity<Object> deleteFile(@PathVariable String question_id,@PathVariable String file_id,Authentication authentication) {
        logger.info("Post Request for create File in Questions");
        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("endpoint.question.file.delete");

        List<User> users = userRepository.findAll();
        User authenticatedUser = null;
        for (User internalUser : users) {
            if (internalUser.getUsername().equals(authentication.getName()))
                authenticatedUser = internalUser;
        }
        if(authenticatedUser==null){
            logger.info("Post Request for create File in Questions didnt work as user not found "+authentication.getName());
            throw new UnauthorizedException("user not found");
        }

        Optional<Question> question = questionRepository.findById(question_id);

        if (!question.isPresent()) {
            logger.info("Post Request for create File in Questions didnt work as question not found "+question_id);
            throw new UserNotFoundException("Question not found" + question_id);
        }

        if(!question.get().getUser_id().equals(authenticatedUser.getUser_id())){
            logger.info("Post Request for create File in Questions didnt work as user not formed the question "+ question_id);
            throw new UnauthorizedException("User has not written this question");
        }

        Optional<Files> file = fileRepository.findById(file_id);

        if (!file.isPresent()) {
            logger.info("file not found");
            throw new UserNotFoundException("file not found" + question_id);
        }
        if(!file.get().getQuestion_id().equals(question_id)) {
            logger.info("file does not belong to  found");
            throw new UnauthorizedException("file does not belong to question");
        }
        long startS3 = System.currentTimeMillis();
        service.deleteFile(file.get().getS3_object_name());
        long endS3 = System.currentTimeMillis();
        long resultS3 = endS3-startS3;
        statsDClient.recordExecutionTime("timer.question.file.delete.S3Service",resultS3);
        long startD = System.currentTimeMillis();
        fileRepository.deleteById(file.get().getFile_id());
        long endD = System.currentTimeMillis();
        long resultD = endD-startD;
        statsDClient.recordExecutionTime("timer.question.database.file.delete",resultD);

        long end = System.currentTimeMillis();
        long result = end-start;
        statsDClient.recordExecutionTime("timer.question.file.delete",result);
        return ResponseEntity.noContent().build();
    }






}
