package com.csye.webapp.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSS3Config {


    @Bean
    public AmazonS3 getAmazonS3Client() {

        System.out.println("get AmazonS3Client from Bean");
        return new AmazonS3Client();
    }
}