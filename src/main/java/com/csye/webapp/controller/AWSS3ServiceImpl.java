package com.csye.webapp.controller;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.csye.webapp.exception.ImproperException;
import com.csye.webapp.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AWSS3ServiceImpl implements AWSS3Service {


    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Override
    // @Async annotation ensures that the method is executed in a different background thread
    // but not consume the main thread.
    @Async
    public void uploadFile(String file_name, final MultipartFile multipartFile) {

        try {
            final File file = convertMultiPartFileToFile(multipartFile);
            System.out.println("++++++++++++++++++++++++++ storing in uploadFile");
            uploadFileToS3Bucket(bucketName,file_name, file);
            file.delete();
        } catch (final AmazonServiceException ex) {
            throw new ImproperException("Some issue while uploading file to due to Conversion from Multipart to file "+ ex);
        }
    }

    private File convertMultiPartFileToFile(final MultipartFile multipartFile) {
        final File file = new File(multipartFile.getOriginalFilename());
        try (final FileOutputStream outputStream = new FileOutputStream(file)) {
            System.out.println("++++++++++++++++++++++++++ storing converting file");
            outputStream.write(multipartFile.getBytes());
        } catch (final IOException ex) {
            throw new ImproperException("Some issue while uploading file to due to IOexception "+ ex);
        }
        return file;
    }

    private void uploadFileToS3Bucket(String bucketName, final String file_name, final File file) {
        amazonS3 = new AmazonS3Client();
        try {

            final PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, file_name, file);
            amazonS3.putObject(putObjectRequest);

        }
        catch(final AmazonServiceException ex){
            throw new ImproperException("Some issue while uploading file to S3 "+ ex);
        }
    }
    @Override
    // @Async annotation ensures that the method is executed in a different background thread
    // but not consume the main thread.
    @Async
    public void deleteFile(final String file_name){
       amazonS3 = new AmazonS3Client();
        try {
            if(!amazonS3.doesObjectExist(bucketName,file_name)){
                throw new UserNotFoundException("The file you are trying to delete does not exist in S3");
            }

            final DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, file_name);
            amazonS3.deleteObject(deleteObjectRequest);
        }
        catch(final AmazonServiceException ex){
            throw new ImproperException("Some issue while deleting file from S3 "+ ex);
        }

    }
}