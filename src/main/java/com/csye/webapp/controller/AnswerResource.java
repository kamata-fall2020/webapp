package com.csye.webapp.controller;

import com.csye.webapp.exception.UnauthorizedException;
import com.csye.webapp.exception.UserNotFoundException;
import com.csye.webapp.model.Answer;
import com.csye.webapp.model.Category;
import com.csye.webapp.model.Question;
import com.csye.webapp.model.User;
import com.csye.webapp.repository.AnswerRepository;
import com.csye.webapp.repository.CategoryRepository;
import com.csye.webapp.repository.QuestionRepository;
import com.csye.webapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class AnswerResource {

    @Autowired
    private UserRepository userRepository;


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



}
