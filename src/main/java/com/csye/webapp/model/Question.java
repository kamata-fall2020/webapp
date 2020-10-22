package com.csye.webapp.model;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.ReadOnlyProperty;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Question {


    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String question_id;

    @ReadOnlyProperty
    private String  user_id;

    @NotNull
    @NotBlank
    private String question_text;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = false)
    @JoinColumn(name = "question_id")
    private List<Answer> answerList = new ArrayList<>();


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = false)
    @JoinColumn(name = "question_id")
    private List<Files> fileList = new ArrayList<>();

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    @ManyToMany(cascade={ CascadeType.DETACH,
            CascadeType.MERGE,
            CascadeType.REFRESH,
            CascadeType.PERSIST})
    private Set<Category> categories = new HashSet<>();

    @ReadOnlyProperty
    private Timestamp question_created;

    @ReadOnlyProperty
    private Timestamp question_updated;

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }


    public String getQuestion_text() {
        return question_text;
    }

    public void setQuestion_text(String question_text) {
        this.question_text = question_text;
    }

    public List<Answer> getAnswerList() {
        return answerList;
    }

    public List<Files> getFileList() {
        return fileList;
    }

    public void setFileList(List<Files> fileList) {
        this.fileList = fileList;
    }

    public void setAnswerList(List<Answer> answerList) {
        this.answerList = answerList;
    }



    public Timestamp getQuestion_created() {
        return question_created;
    }

    public void setQuestion_created(Timestamp question_created) {
        this.question_created = question_created;
    }

    public Timestamp getQuestion_updated() {
        return question_updated;
    }

    public void setQuestion_updated(Timestamp question_updated) {
        this.question_updated = question_updated;
    }

}
