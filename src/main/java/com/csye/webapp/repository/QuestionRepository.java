package com.csye.webapp.repository;

import com.csye.webapp.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, String> {


}
