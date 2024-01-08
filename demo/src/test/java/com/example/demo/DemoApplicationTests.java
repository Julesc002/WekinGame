package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.wekinGame.RunApplication;

@SpringBootTest(classes = RunApplication.class)
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}
	@Test
	void testfailer(){
		assertTrue(1==-1);
	}

 }