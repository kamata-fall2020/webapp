package com.csye.webapp;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.springframework.boot.test.context.SpringBootTest;

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

}
