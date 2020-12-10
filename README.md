# webapp
This is a Spring-boot web application with web services with basic authentication which allows to create user, get information of user and update the details of users.

# Tools used
MySQL Server and workbench for backend
Maven dependencies
Spring Boot Initializer starter project (Spring Boot Framework)
POSTMAN for testing

Java as programming language
Intellij Ultimate as IDE


# Dependencies needed
Spring-Web
Spring-Actuator
Spring DevTools
Spring Security

# Deployment
Build the maven dependencies
Check the properities under application.properties and configure the jdbc driver connection
Check whether the database table and security adapter query matches or not
User structure should match with database table
Run the project

# Steps for running the project
1- go to webapp project folder location on the command line
2- before running the project from commandline, we need to configure the database and project
3- Under application.properties check the configuration of database 
4- in webapp folder command line install maven
5- the command for it is sudo apt install maven
6- sudo apt update 
7- mvn spring-boot:run for running the project
8- APIs can be verified from     Postman 


# steps for importing certificate
Certificate can be uploaded using cli or console
1- For console 
1-Go to Certificate Manager
2-Import a certificate
3-Enter the certificate body, private key and certificate chain
4-Certificate chain is the one which can be found in bundle
5-Click next and submit, you must see that the status of the certificate is issued

2- For cli follow the synopsis

  import-certificate
[--certificate-arn <value>]
--certificate <value>
--private-key <value>
[--certificate-chain <value>]
[--tags <value>]
[--cli-input-json | --cli-input-yaml]
[--generate-cli-skeleton <value>]
 



