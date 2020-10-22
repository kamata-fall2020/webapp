package com.csye.webapp.model;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.ReadOnlyProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Entity
public class Files {


    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String file_id;

    @ReadOnlyProperty
    private String question_id;

    @ReadOnlyProperty
    private String answer_id;

    @ReadOnlyProperty
    private Long size;
    @ReadOnlyProperty
    private String contentType;


    @NotNull
    @NotBlank
    private String s3_object_name;

    public Timestamp getCreated_Date() {
        return created_Date;
    }

    public void setCreated_Date(Timestamp created_Date) {
        this.created_Date = created_Date;
    }

    @ReadOnlyProperty
    private Timestamp created_Date;


    @NotNull
    @NotBlank
    private String file_name;

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFile_id() {
        return file_id;
    }

    public void setFile_id(String file_id) {
        this.file_id = file_id;
    }

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public String getAnswer_id() {
        return answer_id;
    }

    public void setAnswer_id(String answer_id) {
        this.answer_id = answer_id;
    }

    public String getS3_object_name() {
        return s3_object_name;
    }

    public void setS3_object_name(String s3_object_name) {
        this.s3_object_name = s3_object_name;
    }


    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }



}
