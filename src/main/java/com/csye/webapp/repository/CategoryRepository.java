package com.csye.webapp.repository;

import com.csye.webapp.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,String> {


}
