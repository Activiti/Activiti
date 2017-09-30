package org.activiti.services.query.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
	webEnvironment=WebEnvironment.RANDOM_PORT,
	properties={
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.show-sql=true"
	}
)
public class EnableActivitiRestQueryServiceTest {
	
	@SpringBootApplication
	@EnableActivitiRestQueryService
	static class Configuration {
		
	}
	
	@Test
	public void contextLoads() {
		
	}
}
