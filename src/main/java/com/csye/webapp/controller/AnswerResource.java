package com.csye.webapp.controller;

import com.csye.webapp.exception.ImproperException;
import com.csye.webapp.exception.UnauthorizedException;
import com.csye.webapp.exception.UserNotFoundException;
import com.csye.webapp.model.*;
import com.csye.webapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@RestController
public class AnswerResource {

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


    @PostMapping("/v1/question/{question_id}/answer")
    public Answer answerQuestion(@PathVariable String question_id, @Valid @RequestBody Answer answer, Authentication authentication) throws UserNotFoundException {
        List<User> users = userRepository.findAll();
        User authenticatedUser = null;
        for (User internalUser : users) {
            if (internalUser.getUsername().equals(authentication.getName()))
                authenticatedUser = internalUser;
        }
        if(authenticatedUser==null)
            throw new UnauthorizedException("id-" + authentication.getName());

        Optional<Question> question = questionRepository.findById(question_id);
        if (!question.isPresent())
            throw new UserNotFoundException("question is  not present" + question_id);

        answer.setQuestion_id(question_id);
        answer.setUser_id(authenticatedUser.getUser_id());
        answer.setAnswer_created(new Timestamp(System.currentTimeMillis()));
        answer.setAnswer_updated(new Timestamp(System.currentTimeMillis()));
        List<Answer> answerList = question.get().getAnswerList();
        answerList.add(answer);
        question.get().setAnswerList(answerList);

        answerRepository.save(answer);

        return answer;
    }

    @DeleteMapping("/v1/question/{question_id}/answer/{answer_id}")
    public ResponseEntity<Object> deleteAnswer(@PathVariable String question_id,@PathVariable String answer_id, Authentication authentication) {
        List<User> users = userRepository.findAll();
        User authenticatedUser = null;
        for (User internalUser : users) {
            if (internalUser.getUsername().equals(authentication.getName()))
                authenticatedUser = internalUser;
        }
        Optional<Answer> answer = answerRepository.findById(answer_id);
        if(authenticatedUser==null){
            throw new UnauthorizedException("user not found");
        }
        if (!answer.get().getUser_id().equals(authenticatedUser.getUser_id())){
            throw new UnauthorizedException("you cannot delete this answer" + question_id);
        }
        Optional<Question> question = questionRepository.findById(question_id);
        if (!question.isPresent()) {
            throw new UserNotFoundException("Question not present" + question_id);
        }
        if(!answer.isPresent()) {
                throw new UserNotFoundException("answer not present " + question_id);
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
                        service.deleteFile(files.getS3_object_name());
                }

            }
        }
                if(flag==0){
                    throw new UnauthorizedException("answer does not belong to specific question" + question_id);
                }

        answerRepository.deleteById(answer_id);
        question.get().setAnswerList(questionAnswerList);
        questionRepository.save(question.get());

        return ResponseEntity.noContent().build();
    }


    @PutMapping("/v1/question/{question_id}/answer/{answer_id}")
    public ResponseEntity<Object> updateAnswer(@PathVariable String question_id,@PathVariable String answer_id,@Valid @RequestBody Answer answer, Authentication authentication){
        List<User> users = userRepository.findAll();
        User authenticatedUser = null;
        for(User internalUser : users){
            if(internalUser.getUsername().equals(authentication.getName()))
                authenticatedUser = internalUser;
        }
        if(authenticatedUser==null){
            throw new UnauthorizedException("user not found");
        }
        Optional<Question> question = questionRepository.findById(question_id);
        if (!question.isPresent()) {
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

        answerRepository.save(answer);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/v1/question/{question_id}/answer/{answer_id}")
    public Answer updateAnswer(@PathVariable String question_id,@PathVariable String answer_id){
        Optional<Question> question = questionRepository.findById(question_id);
        if (!question.isPresent()) {
            throw new UserNotFoundException("question is not present" + question_id);
        }
            Optional<Answer> answerById = answerRepository.findById(answer_id);
            if(!answerById.isPresent()){
                throw new UserNotFoundException("answer not present" + question_id);
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

        return answerById.get();
    }


    @PostMapping("/v1/question/{question_id}/answer/{answer_id}/file")
    public Files createFile(@PathVariable String question_id, @PathVariable String answer_id, @RequestPart(value = "file") MultipartFile fileInput, Authentication authentication){
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
        Optional<Question> question = questionRepository.findById(question_id);
        if (!question.isPresent()) {
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
                            service.uploadFile(file_name,fileInput);

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
                        fileRepository.save(file);
                        //fileModel=file;
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

        return file;
    }


    @DeleteMapping("/v1/question/{question_id}/answer/{answer_id}/file/{file_id}")
    public ResponseEntity<Object>  deleteFile(@PathVariable String question_id, @PathVariable String answer_id,@PathVariable String file_id, Authentication authentication){
        List<User> users = userRepository.findAll();
        User authenticatedUser = null;
        for(User internalUser : users){
            if(internalUser.getUsername().equals(authentication.getName()))
                authenticatedUser = internalUser;
        }
        if(authenticatedUser==null){
            throw new UnauthorizedException("user not found");
        }
        Optional<Question> question = questionRepository.findById(question_id);
        if (!question.isPresent()) {
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
                        Optional<Files> file = fileRepository.findById(file_id);

                        if (!file.isPresent())
                            throw new UserNotFoundException("file not found" + question_id);
                        if(!file.get().getAnswer_id().equals(answer_id))
                            throw new UnauthorizedException("file does not belong to answer");
                        service.deleteFile(file.get().getS3_object_name());
                        fileRepository.deleteById(file.get().getFile_id());
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

        return ResponseEntity.noContent().build();
    }



}
