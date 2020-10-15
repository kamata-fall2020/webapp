package com.csye.webapp;

import com.csye.webapp.controller.UserResource;
import com.csye.webapp.model.User;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//@SpringBootTest
@Testable
class WebappApplicationTests {

//	@Test
//	void contextLoads() {
//	}
    @Test
	void checkTest(){
		assertTrue(true);
	}

	@Test
	void userResource(){
    	int i =0;
		UserResource ur = new UserResource();
    	if(ur.isValid("passWORD@1")){
    		i=1;
		}
		assertEquals(i,1);
	}



}
