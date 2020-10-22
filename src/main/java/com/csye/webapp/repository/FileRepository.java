package com.csye.webapp.repository;

import com.csye.webapp.model.Files;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<Files,String> {


}
