package com.csye.webapp.controller;


import org.springframework.web.multipart.MultipartFile;

public interface AWSS3Service {

    void uploadFile(String file_name, MultipartFile multipartFile);

    void deleteFile(String s3_object_name);
}