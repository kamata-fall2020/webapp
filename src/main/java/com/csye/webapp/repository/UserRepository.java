package com.csye.webapp.repository;

import com.csye.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,String> {




}
