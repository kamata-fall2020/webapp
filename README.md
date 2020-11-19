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
