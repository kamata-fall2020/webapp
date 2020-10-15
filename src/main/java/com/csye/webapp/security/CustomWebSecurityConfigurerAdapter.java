package com.csye.webapp.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class CustomWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter{

    @Autowired
    private DataSource dataSource;

    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder authBuilder) throws Exception {

        authBuilder.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(new BCryptPasswordEncoder())
                .usersByUsernameQuery("select username, password, enabled from user where username=?")
               .authoritiesByUsernameQuery("select username, password, enabled from user where username=?");



    }



    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.csrf().disable().cors().disable().authorizeRequests().antMatchers("/v1/user/self").authenticated()
                .and().authorizeRequests().antMatchers(HttpMethod.POST,"/v1/question/**").authenticated()
                .and().authorizeRequests().antMatchers(HttpMethod.DELETE,"/v1/question/**").authenticated()
                .and().authorizeRequests().antMatchers(HttpMethod.DELETE,"/v1/question/**/answer/**").authenticated()
                .and().authorizeRequests().antMatchers(HttpMethod.PUT,"/v1/question/**").authenticated()
                .and().authorizeRequests().antMatchers(HttpMethod.PUT,"/v1/question/**/answer/**").authenticated()
                .and().httpBasic();
//                .and().formLogin().permitAll()
//                .and().logout().permitAll();



    }
}
