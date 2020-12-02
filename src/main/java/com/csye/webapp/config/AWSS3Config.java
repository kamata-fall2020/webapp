package com.csye.webapp.config;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;

@Configuration
public class AWSS3Config {


    @Value("${aws.region}")
    String region;

    @Value("${aws.profile}")
    String profile;


    @Bean
    public AmazonS3 getAmazonS3Client() {

        System.out.println("get AmazonS3Client from Bean");
        return new AmazonS3Client();
    }


    @Bean
    public AmazonSNS amazonSNS() {
        AWSCredentialsProviderChain awsCredentialsProviderChain = new AWSCredentialsProviderChain(
                new InstanceProfileCredentialsProvider(true),
                new ProfileCredentialsProvider(profile)
        );
        return AmazonSNSClientBuilder.standard()
                .withCredentials(awsCredentialsProviderChain)
                .withRegion(region)
                .build();
    }
}