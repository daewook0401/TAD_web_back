package com.tad.www;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class WwwApplicationTests {

	@MockitoBean
	JavaMailSender javaMailSender;

	@Test
	void contextLoads() {
	}

}
