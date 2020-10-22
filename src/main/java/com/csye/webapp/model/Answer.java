package com.csye.webapp.model;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.ReadOnlyProperty;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Answer {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String answer_id;
    @ReadOnlyProperty
    private String user_id;
    @ReadOnlyProperty
    private String question_id;

    private String answer_text;



    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = false)
    @JoinColumn(name = "answer_id")
    private List<Files> fileList = new ArrayList<>();


    @ReadOnlyProperty
    private Timestamp answer_created;

    @ReadOnlyProperty
    private Timestamp answer_updated;


    public Timestamp getAnswer_created() {
        return answer_created;
    }

    public void setAnswer_created(Timestamp answer_created) {
        this.answer_created = answer_created;
    }

    public Timestamp getAnswer_updated() {
        return answer_updated;
    }

    public void setAnswer_updated(Timestamp answer_updated) {
        this.answer_updated = answer_updated;
    }

    public String getAnswer_id() {
        return answer_id;
    }

    public void setAnswer_id(String answer_id) {
        this.answer_id = answer_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public String getAnswer_text() {
        return answer_text;
    }

    public void setAnswer_text(String answer_text) {
        this.answer_text = answer_text;
    }

    public List<Files> getFileList() {
        return fileList;
    }

    public void setFileList(List<Files> fileList) {
        this.fileList = fileList;
    }


}
