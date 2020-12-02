package com.csye.webapp.controller;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.csye.webapp.exception.ImproperException;
import com.csye.webapp.exception.UnauthorizedException;
import com.csye.webapp.exception.UserNotFoundException;
import com.csye.webapp.model.*;
import com.csye.webapp.repository.*;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

@RestController
public class AnswerResource {

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

    @Autowired
    private AmazonSNS amazonSNS;

    @Value("${webapp.domain}")
    private String webappDomain;

    @Value("${sns.topic.arn}")
    private String snsTopicArn;


    @PostMapping("/v1/question/{question_id}/answer")
    public Answer answerQuestion(@PathVariable String question_id, @Valid @RequestBody Answer answer, Authentication authentication) throws UserNotFoundException {
        logger.info("POST Request for Create Answer ");

        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("endpoint.answer.http.post");

        List<User> users = userRepository.findAll();
        User authenticatedUser = null;
        for (User internalUser : users) {
            if (internalUser.getUsername().equals(authentication.getName()))
                authenticatedUser = internalUser;
        }
        if(authenticatedUser==null) {
            logger.info("User logged in is invalid. Username is "+authentication.getName());
            throw new UnauthorizedException("id-" + authentication.getName());

        }

        Optional<Question> question = questionRepository.findById(question_id);
        if (!question.isPresent()) {
            logger.info("QuestionID passed is not present "+question_id);
            throw new UserNotFoundException("question is  not present" + question_id);
        }

        answer.setQuestion_id(question_id);
        answer.setUser_id(authenticatedUser.getUser_id());
        answer.setAnswer_created(new Timestamp(System.currentTimeMillis()));
        answer.setAnswer_updated(new Timestamp(System.currentTimeMillis()));
        List<Answer> answerList = question.get().getAnswerList();
        answerList.add(answer);

        question.get().setAnswerList(answerList);
        long startD = System.currentTimeMillis();

        answerRepository.save(answer);

        long endD = System.currentTimeMillis();
        long resultD = endD-startD;
        statsDClient.recordExecutionTime("timer.answer.database.post",resultD);

        long end = System.currentTimeMillis();
        long result = end-start;
        statsDClient.recordExecutionTime("timer.answer.post",result);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("from", "noreply@"+webappDomain);
        jsonObject.put("to", "kamat1.aditya@gmail.com");
        jsonObject.put("QuestionID",question_id);
        jsonObject.put("AnswerText",answer.getAnswer_text());
        jsonObject.put("AnswerID",answer.getAnswer_id());
        jsonObject.put("Message", "The answer has been given to this specific question ID");
        jsonObject.put("AnswerLink","http://" + webappDomain + "/v1/question/" +question_id+"/answer/"+ answer.getAnswer_id() );
        logger.info("JSON string created: " + jsonObject.toString());
        logger.info("Publishing the message to SNS...");

        PublishResult publishResult = amazonSNS.publish(new PublishRequest(snsTopicArn, jsonObject.toString()));

        logger.info("SNS message published: " + publishResult.toString());
        return answer;
    }

    @DeleteMapping("/v1/question/{question_id}/answer/{answer_id}")
    public ResponseEntity<Object> deleteAnswer(@PathVariable String question_id,@PathVariable String answer_id, Authentication authentication) {
        logger.info("DELETE  Request for  Answer ");

        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("endpoint.answer.http.delete");

        List<User> users = userRepository.findAll();
        User authenticatedUser = null;
        for (User internalUser : users) {
            if (internalUser.getUsername().equals(authentication.getName()))
                authenticatedUser = internalUser;
        }
        Optional<Answer> answer = answerRepository.findById(answer_id);
        if(authenticatedUser==null){
            logger.info("DELETE  Request for  Answer didnt work as user is not found in system "+authentication.getName());
            throw new UnauthorizedException("user not found");
        }
        if (!answer.get().getUser_id().equals(authenticatedUser.getUser_id())){
            logger.info("DELETE  Request for  Answer didnt work as user is not allowed to delete "+authentication.getName());
            throw new UnauthorizedException("you cannot delete this answer" + question_id);
        }
        Optional<Question> question = questionRepository.findById(question_id);
        if (!question.isPresent()) {
            logger.info("DELETE  Request for  Answer didnt work as question id is not present "+question_id);
            throw new UserNotFoundException("Question not present" + question_id);
        }
        if(!answer.isPresent()) {
              logger.info("DELETE  Request for  Answer didnt work as answer id is not present "+answer_id);
                throw new UserNotFoundException("answer not present " + answer_id);
        }
                List<Answer> questionAnswerList = question.get().getAnswerList();
                int flag = 0;

        for (int i = 0; i < questionAnswerList.size(); ++i) {

            if (questionAnswerList.get(i).getAnswer_id().equals(answer.get().getAnswer_id())) {
                flag = 1;
                questionAnswerList.remove(i);
                List<Files> file = fileRepository.findAll();
                for (Files files : file) {
                    if (files.getAnswer_id().equals(answer_id))
                        // authenticatedUser = internalUser;
                    {
                        long startS3 = System.currentTimeMillis();
                        service.deleteFile(files.getS3_object_name());
                        long endS3 = System.currentTimeMillis();
                        long resultS3 = endS3-startS3;
                        statsDClient.recordExecutionTime("timer.answer.delete.S3Service",resultS3);

                    }
                }

            }
        }
                if(flag==0){
                    throw new UnauthorizedException("answer does not belong to specific question" + question_id);
                }

        long startD = System.currentTimeMillis();

        answerRepository.deleteById(answer_id);
        question.get().setAnswerList(questionAnswerList);
        questionRepository.save(question.get());

        long endD = System.currentTimeMillis();
        long resultD = endD-startD;
        statsDClient.recordExecutionTime("timer.answer.database.delete",resultD);

        long end = System.currentTimeMillis();
        long result = end-start;
        statsDClient.recordExecutionTime("timer.answer.delete",result);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("from", "noreply@"+webappDomain);
        jsonObject.put("to", "kamat1.aditya@gmail.com");
        jsonObject.put("QuestionID",question_id);
        jsonObject.put("AnswerID",answer_id);
        jsonObject.put("Message", "The answer has been deleted to this specific question ID");
      //  jsonObject.put("AnswerLink","http://" + webappDomain + "/v1/question/" +question_id+"/answer/"+ answer.getAnswer_id() );
        logger.info("JSON string created: " + jsonObject.toString());
        logger.info("Publishing the message to SNS...");

        PublishResult publishResult = amazonSNS.publish(new PublishRequest(snsTopicArn, jsonObject.toString()));

        logger.info("SNS message published: " + publishResult.toString());
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/v1/question/{question_id}/answer/{answer_id}")
    public ResponseEntity<Object> updateAnswer(@PathVariable String question_id,@PathVariable String answer_id,@Valid @RequestBody Answer answer, Authentication authentication){
        logger.info("PUT  Request for  Answer ");

        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("endpoint.answer.http.put");

        List<User> users = userRepository.findAll();
        User authenticatedUser = null;
        for(User internalUser : users){
            if(internalUser.getUsername().equals(authentication.getName()))
                authenticatedUser = internalUser;
        }
        if(authenticatedUser==null){
            logger.info("PUT  Request didnt work as user not found"+authentication.getName());
            throw new UnauthorizedException("user not found");
        }
        Optional<Question> question = questionRepository.findById(question_id);
        if (!question.isPresent()) {
            logger.info("PUT  Request didnt work as question not found "+question_id);
            throw new UserNotFoundException("question is not present" + question_id);
        }else{
            Optional<Answer> answerById = answerRepository.findById(answer_id);
            if(!answerById.isPresent()){
                logger.info("PUT  Request didnt work as answer not found "+answer_id);
                throw new UserNotFoundException("answer not present" + answer_id);
            }else {
                List<Answer> questionAnswerList = question.get().getAnswerList();
                int flag = 0;
                for (Answer ans : questionAnswerList){
                    if (ans.getAnswer_id().equals(answerById.get().getAnswer_id())){
                        flag = 1;
                        answer.setQuestion_id(ans.getQuestion_id());
                        answer.setAnswer_created(ans.getAnswer_created());
                    }

                }
                if(flag==0){
                    throw new UnauthorizedException("answer does not belong to specific question" + question_id);
                }
            }

            if (!answerById.get().getUser_id().equals(authenticatedUser.getUser_id())){
                throw new UnauthorizedException("you cannot update this answer" + question_id);
            }
        }
        answer.setAnswer_id(answer_id);
        answer.setUser_id(authenticatedUser.getUser_id());
        answer.setAnswer_updated(new Timestamp(System.currentTimeMillis()));
        long startD = System.currentTimeMillis();

        answerRepository.save(answer);

        long endD = System.currentTimeMillis();
        long resultD = endD-startD;
        statsDClient.recordExecutionTime("timer.answer.database.put",resultD);

        long end = System.currentTimeMillis();
        long result = end-start;
        statsDClient.recordExecutionTime("timer.answer.put",result);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("from", "noreply@"+webappDomain);
        jsonObject.put("to", "kamat1.aditya@gmail.com");
        jsonObject.put("QuestionID",question_id);
        jsonObject.put("AnswerID",answer_id);
        jsonObject.put("AnswerText",answer.getAnswer_text());
        jsonObject.put("Message", "The answer has been updated to this specific question ID");
        jsonObject.put("AnswerLink","http://" + webappDomain + "/v1/question/" +question_id+"/answer/"+ answer.getAnswer_id() );
        logger.info("JSON string created: " + jsonObject.toString());
        logger.info("Publishing the message to SNS...");

        PublishResult publishResult = amazonSNS.publish(new PublishRequest(snsTopicArn, jsonObject.toString()));

        logger.info("SNS message published: " + publishResult.toString());


        return ResponseEntity.noContent().build();
    }

    @GetMapping("/v1/question/{question_id}/answer/{answer_id}")
    public Answer updateAnswer(@PathVariable String question_id,@PathVariable String answer_id){
        logger.info("GET  Request for  Answer by id");

        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("endpoint.answer.http.get");
        Optional<Question> question = questionRepository.findById(question_id);
        if (!question.isPresent()) {
            logger.info("GET  Request for  question didnt work as  questionid not present "+ question_id);
            throw new UserNotFoundException("question is not present" + question_id);
        }
            Optional<Answer> answerById = answerRepository.findById(answer_id);
            if(!answerById.isPresent()){
                logger.info("GET  Request for  question didnt work as  amswerid not present "+ answer_id);
                throw new UserNotFoundException("answer not present" + answer_id);
            }else {
                List<Answer> questionAnswerList = question.get().getAnswerList();
                int flag = 0;
                for (Answer ans : questionAnswerList){
                    if (ans.getAnswer_id().equals(answerById.get().getAnswer_id())){
                        flag = 1;
                    }

                }
                if(flag==0){
                    throw new UnauthorizedException("answer does not belong to specific question" + question_id);
                }
            }


        long end = System.currentTimeMillis();
        long result = end-start;
        statsDClient.recordExecutionTime("timer.answer.get",result);
        return answerById.get();
    }


    @PostMapping("/v1/question/{question_id}/answer/{answer_id}/file")
    public Files createFile(@PathVariable String question_id, @PathVariable String answer_id, @RequestPart(value = "file") MultipartFile fileInput, Authentication authentication){
        logger.info("POST  Request for  Answer File by id");

        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("endpoint.answer.file.http.post");

        List<User> users = userRepository.findAll();
        Files file = new Files();
        User authenticatedUser = null;
        for(User internalUser : users){
            if(internalUser.getUsername().equals(authentication.getName()))
                authenticatedUser = internalUser;
        }
        if(authenticatedUser==null){
            logger.info("POST  Request for  Answer File didnt work as user not found "+authentication.getName());
            throw new UnauthorizedException("user not found");
        }
        Optional<Question> question = questionRepository.findById(question_id);
        if (!question.isPresent()) {
            logger.info("POST  Request for  Answer File didnt work as question not found "+question_id);
            throw new UserNotFoundException("question is not present" + question_id);
        }else{
            Optional<Answer> answerById = answerRepository.findById(answer_id);
            if(!answerById.isPresent()){
                throw new UserNotFoundException("answer not present" + question_id);
            }else {
                List<Answer> questionAnswerList = question.get().getAnswerList();
                int flag = 0;
                for (Answer ans : questionAnswerList){
                    if (ans.getAnswer_id().equals(answerById.get().getAnswer_id())){
                        flag = 1;
                        //Files file = new Files();
                        String file_name ="";
                        Timestamp date = new Timestamp(System.currentTimeMillis());

                        try {

                            if (fileInput.getOriginalFilename().isEmpty()){
                                throw new UserNotFoundException("file is not present");
                            }
                            file_name = answer_id+"/"+date.toString()+"/"+fileInput.getOriginalFilename().replace(" ", "_");

                            long startS3 = System.currentTimeMillis();

                            service.uploadFile(file_name,fileInput);

                            long endS3 = System.currentTimeMillis();
                            long resultS3 = endS3-startS3;
                            statsDClient.recordExecutionTime("timer.answer.file.post.S3Service",resultS3);

                        } catch (Exception e) {

                            throw new ImproperException("Some issue while processing file");
                        }
                        file.setAnswer_id(answer_id);
                       // file.setQuestion_id(question_id);
                        file.setCreated_Date(date);
                        file.setContentType(fileInput.getContentType());
                        file.setSize(fileInput.getSize());
                        file.setFile_name(fileInput.getOriginalFilename().replace(" ", "_"));
                        file.setS3_object_name(file_name);

                        long startD = System.currentTimeMillis();

                        fileRepository.save(file);
                        //fileModel=file;
                        long endD = System.currentTimeMillis();
                        long resultD = endD-startD;
                        statsDClient.recordExecutionTime("timer.answer.database.file.post",resultD);
                    }

                }
                if(flag==0){
                    throw new UnauthorizedException("answer does not belong to specific question" + question_id);
                }
            }

            if (!answerById.get().getUser_id().equals(authenticatedUser.getUser_id())){
                throw new UnauthorizedException("you cannot update this answer" + question_id);
            }
        }

        long end = System.currentTimeMillis();
        long result = end-start;
        statsDClient.recordExecutionTime("timer.answer.file.post",result);
        return file;
    }


    @DeleteMapping("/v1/question/{question_id}/answer/{answer_id}/file/{file_id}")
    public ResponseEntity<Object>  deleteFile(@PathVariable String question_id, @PathVariable String answer_id,@PathVariable String file_id, Authentication authentication){
        logger.info("DELETE  Request for  Answer File by id");

        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("endpoint.answer.file.http.delete");
        List<User> users = userRepository.findAll();
        User authenticatedUser = null;
        for(User internalUser : users){
            if(internalUser.getUsername().equals(authentication.getName()))
                authenticatedUser = internalUser;
        }
        if(authenticatedUser==null){
            logger.info("DELETE  Request for  Answer didnt work as user not found "+authentication.getName());
            throw new UnauthorizedException("user not found");
        }
        Optional<Question> question = questionRepository.findById(question_id);
        if (!question.isPresent()) {
            logger.info("DELETE  Request for  Answer didnt work as questionid is not present "+question_id);
            throw new UserNotFoundException("question is not present" + question_id);
        }else{
            Optional<Answer> answerById = answerRepository.findById(answer_id);
            if(!answerById.isPresent()){
                logger.info("DELETE  Request for  Answer didnt work as answerid is not present "+answer_id);
                throw new UserNotFoundException("answer not present" + answer_id);
            }else {
                List<Answer> questionAnswerList = question.get().getAnswerList();
                int flag = 0;
                for (Answer ans : questionAnswerList){
                    if (ans.getAnswer_id().equals(answerById.get().getAnswer_id())){
                        flag = 1;
                        Optional<Files> file = fileRepository.findById(file_id);

                        if (!file.isPresent())
                            throw new UserNotFoundException("file not found" + question_id);
                        if(!file.get().getAnswer_id().equals(answer_id))
                            throw new UnauthorizedException("file does not belong to answer");

                        long startS3 = System.currentTimeMillis();

                        service.deleteFile(file.get().getS3_object_name());

                        long endS3 = System.currentTimeMillis();
                        long resultS3 = endS3-startS3;
                        statsDClient.recordExecutionTime("timer.answer.file.delete",resultS3);

                        long startD = System.currentTimeMillis();
                        fileRepository.deleteById(file.get().getFile_id());
                        long endD = System.currentTimeMillis();
                        long resultD = endD-startD;
                        statsDClient.recordExecutionTime("timer.answer.file.delete",resultD);
                    }

                }
                if(flag==0){
                    throw new UnauthorizedException("answer does not belong to specific question" + question_id);
                }
            }

            if (!answerById.get().getUser_id().equals(authenticatedUser.getUser_id())){
                throw new UnauthorizedException("you cannot delete this file " + file_id);
            }
        }
        long end = System.currentTimeMillis();
        long result = end-start;
        statsDClient.recordExecutionTime("timer.answer.file.delete",result);
        return ResponseEntity.noContent().build();
    }



    private String constructRecipeURL(String recipeId) {
        return ("https://" + webappDomain + "/v1/recipe/" + recipeId);
    }




}
